import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest // Importar para atualizar perfil
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirebaseRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance().reference // storage root


    suspend fun uploadProfileImage(userId: String, imageUri: Uri?): String? = withContext(Dispatchers.IO) {
        if (imageUri == null) {
            System.out.println("uploadProfileImage: imageUri é null, retornando null.")
            return@withContext null
        }
        val ref = storage.child("profile_images/$userId.jpg") // Ou o caminho que você usa
        return@withContext try {
            println("uploadProfileImage: Tentando upload para ${ref.path}")
            ref.putFile(imageUri).await() // Espera o upload completar
            println("uploadProfileImage: Upload completo. Tentando obter URL de download.")
            val downloadUrl = ref.downloadUrl.await().toString() // Espera a URL de download
            println("uploadProfileImage: URL de download obtida: $downloadUrl")
            downloadUrl // Retorna a URL
        } catch (e: Exception) {
            System.err.println("uploadProfileImage: Erro durante o upload ou obtenção da URL: ${e.message}")
            e.printStackTrace() // IMPORTANTE: Veja o stack trace completo
            null // Retorna null em caso de erro
        }
    }

    suspend fun registerUserAndCreateDocument(
        name: String,
        email: String,
        password: String,
        profileImageUri: Uri?
    ): Result<FirebaseUser> { // Usando a classe Result que sugeri antes
        return withContext(Dispatchers.IO) { // Executar toda a lógica em background
            try {
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val firebaseUser = authResult.user
                    ?: return@withContext Result.failure(Exception("Usuário do Firebase nulo após criação."))

                val imageUrl = if (profileImageUri != null) {
                    uploadProfileImage(firebaseUser.uid, profileImageUri)
                } else {
                    null
                }

                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .apply { imageUrl?.let { setPhotoUri(Uri.parse(it)) } } // Adiciona photoUri se imageUrl não for nulo
                    .build()
                firebaseUser.updateProfile(profileUpdates).await()


                val userMap = hashMapOf(
                    "uid" to firebaseUser.uid,
                    "name" to name, // ou firebaseUser.displayName se confiar que foi atualizado
                    "email" to email, // ou firebaseUser.email
                    "profileImageUrl" to imageUrl,
                    "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                )
                db.collection("users").document(firebaseUser.uid)
                    .set(userMap)
                    .await()

                Result.success(firebaseUser)

            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
