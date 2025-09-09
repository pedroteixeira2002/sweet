# Relatório do Projeto - Sweet

## 1. Contexto do Tema

O projeto **Sweet** consiste numa aplicação Android desenvolvida em Kotlin, focada na localização e avaliação de estabelecimentos (como pastelarias, cafés, etc). O objetivo é permitir que utilizadores visualizem, avaliem e explorem estabelecimentos próximos, facilitando a tomada de decisão sobre onde ir. O contexto surge da necessidade de tornar a experiência de descoberta local mais interativa, personalizada e social, aliando funcionalidades de mapa, reviews e leaderboards.

## 2. Funcionalidades Implementadas

- **Localização do Utilizador e Mapa Interativo:** A app utiliza o Google Maps para mostrar a localização do utilizador e dos estabelecimentos próximos. O mapa centraliza na posição do utilizador e adiciona marcadores para cada estabelecimento, permitindo visualizar detalhes ao clicar.
- **Barra de Pesquisa de Endereço:** Permite encontrar estabelecimentos por endereço, com sugestões dinâmicas (Google Places API).
- **Listagem de Estabelecimentos:** Os estabelecimentos próximos são apresentados numa lista (LazyColumn), com cards clicáveis para abrir detalhes.
- **Detalhes do Estabelecimento:** Tela com informações detalhadas, fotos (via Coil), avaliações e reviews.
- **Sistema de Avaliações:** Utilizadores autenticados podem adicionar reviews aos estabelecimentos, incluindo fotos, comentário avaliação e faixa de preço.
- **Leaderboard:** Ranking dos estabelecimentos com melhores avaliações.
- **Gestão de Permissões:** Diálogos informativos para permissões de localização (rationale e negação permanente).
- **Notificações e Geofences:** Notificações baseadas em eventos de localização, usando Geofences para alertar quando o utilizador está perto de estabelecimentos.
- **Perfil de Utilizador:** Área do utilizador para ver e editar perfil, ver as suas avaliações e estabelecimentos adicionados.
- **Configurações:** Suporte a dark mode e escolha de idioma.

## 3. Componentes Android Utilizados

- **Activities e Fragments:** `MainActivity` faz a gestão principal, delegando para composables e view models as várias telas.
- **Jetpack Compose:** Utilizado para toda a interface, com componentes como `BottomSheetScaffold`, `LazyColumn`, `AlertDialog`, `Slider`, etc.
- **ViewModel:** Separação da lógica de negócio e dados da UI (`HomeViewModel`, `ProfileViewModel`, `AuthViewModel`, etc).
- **LiveData/StateFlow:** Estado reativo para atualização da UI conforme dados mudam.
- **Google Maps e Places API:** Para mapas, marcadores, procura de endereços e sugestões.
- **Room Database:** Para persistência local dos dados de estabelecimentos, reviews e utilizadores (ver `SweetDatabase.kt`).
- **Geofences:** Utilização do `GeofencingClient` e broadcast receivers para eventos de proximidade.
- **Navigation Component (Compose):** Implementação de navegação entre telas/flows.
- **Material3:** Para visual e temas, com cores e tipografia custom.

## 4. Bibliotecas Externas Utilizadas

Analisando imports e inicializações:
- **Google Maps Compose:** Para integração de mapas na UI.
- **Google Places API:** Sugestões de endereço e conversão de texto para coordenadas.
- **Firebase (Firestore, Auth, Storage):** Autenticação, persistência e sincronização de dados, além de upload de fotos de reviews.
- **Room:** Persistência local de dados.
- **Coil:** Carregamento eficiente de imagens nas telas de detalhes.
- **Timber:** Logging estruturado durante o desenvolvimento e debug.
- **Kotlin Coroutines:** Programação assíncrona, coleta de dados e atualização de estado.
- **Material3:** UI moderna e responsiva.
- **DataStore:** Persistência de configurações como idioma e dark mode.

## 5. Funcionamento da Aplicação

### Fluxo principal:
1. **Startup:** Ao abrir, solicita permissões de localização. Se concedidas, inicializa o mapa e centraliza na posição do utilizador.
2. **Map e Listagem:** Mostra estabelecimentos próximos no mapa (marcadores) e em lista. O raio de procura pode ser ajustado.
3. **Pesquisa:** Barra de pesquisa permite filtrar estabelecimentos por endereço.
4. **Detalhes:** Ao clicar num card ou marcador, abre a tela de detalhes com fotos, reviews, info e opção de avaliar.
5. **Avaliação:** Utilizador pode adicionar review, comentário e foto. Só permitido se estiver autenticado e próximo ao local.
6. **Leaderboard:** Estabelecimentos são ranked por média de avaliações.
7. **Geofences:** Notificações quando o utilizador entra na proximidade de um estabelecimento.
8. **Perfil:** Utilizador pode ver/editar perfil, ver as suas avaliações e estabelecimentos que registou.
9. **Configurações:** Alterar idioma e tema.

### Fluxos alternativos:
- Diálogos de permissão aparecem quando necessário.
- Tratamento de erros (ex: localização não obtida, permissões negadas).

## 6. Decisões de Implementação

- **Kotlin + Jetpack Compose:**
- **Arquitetura MVVM:** 
- **Persistência híbrida (Room + Firestore):** 
- **Google APIs para localização e mapas:** 
- **Firebase Auth para login:** 
- **Uso extensivo de coroutines:**
- **Material3:** 
- **Design centrado no utilizador:** 

## 7. Resultados Obtidos

- **Funcionamento fluido:** A interface responde rapidamente às ações do utilizador.
- **Mapas interativos e precisos:** Centralização na localização, filtragem por raio, procura por endereço.
- **Avaliações e reviews:** Utilizadores podem contribuir ativamente para a comunidade.
- **Notificações úteis:** Alertas de proximidade, informações relevantes.
- **Perfil e leaderboard:** Incentivo à participação via rankings e personalização.

## 8. Conclusões

A aplicação **Sweet** demonstra um uso avançado das tecnologias Android, proporcionando uma experiência rica e interativa para descoberta e avaliação de estabelecimentos locais. O uso de arquitetura MVVM, integração com APIs modernas e bibliotecas robustas garante escalabilidade e facilidade de manutenção. As decisões de design e implementação refletem boas práticas aprendidas nas aulas, colocando o utilizador no centro da experiência.

**Pontos fortes:**  
- Integração completa com localização, mapas e reviews.
- UI fluida e moderna.
- Suporte offline e sincronização.
- Gamificação via leaderboard.

**Pontos a melhorar:**  
- Expandir tipos de estabelecimentos, filtros avançados.
- Notificações contextuais mais inteligentes.
- Melhorar onboarding e tutorial inicial.
- Terminar suporte à multilinguagem 
