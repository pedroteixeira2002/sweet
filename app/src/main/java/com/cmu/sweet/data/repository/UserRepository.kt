// UserRepository.kt
import android.net.Uri
import com.google.firebase.auth.FirebaseUser


class UserRepository(private val firebaseRepository: FirebaseRepository) {

    suspend fun registerUser(
        name: String,
        email: String,
        password: String,
        profileImageUri: Uri?
    ): Result<FirebaseUser> {
        return firebaseRepository.registerUserAndCreateDocument(
            name,
            email,
            password,
            profileImageUri
        )
    }
}
