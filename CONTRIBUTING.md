# Contributing to Sweet 🍯

Thank you for considering contributing to Sweet! This document provides guidelines and instructions for contributing to this location-based Android application.

## 📋 Table of Contents

- [Code of Conduct](#code-of-conduct)
- [How Can I Contribute?](#how-can-i-contribute)
- [Development Setup](#development-setup)
- [Project Structure](#project-structure)
- [Coding Standards](#coding-standards)
- [Commit Guidelines](#commit-guidelines)
- [Pull Request Process](#pull-request-process)
- [Testing Guidelines](#testing-guidelines)
- [Documentation](#documentation)

## 🤝 Code of Conduct

By participating in this project, you agree to maintain a respectful and inclusive environment. Please be considerate of others and follow professional communication standards.

## 🛠️ How Can I Contribute?

### Reporting Bugs
1. **Check existing issues** first to avoid duplicates
2. **Use the bug report template** when creating new issues
3. **Provide detailed information**:
   - Device information (OS version, device model)
   - Steps to reproduce the issue
   - Expected vs actual behavior
   - Screenshots or logs if applicable

### Suggesting Enhancements
1. **Check if the enhancement already exists** in issues or discussions
2. **Describe the enhancement** with clear use cases
3. **Explain why this enhancement would be useful** to users
4. **Consider the impact** on existing functionality

### Contributing Code
1. **Fork the repository** and create a feature branch
2. **Follow the development setup** instructions
3. **Implement your changes** following coding standards
4. **Write tests** for new functionality
5. **Update documentation** as needed
6. **Submit a pull request**

## 🔧 Development Setup

### Prerequisites
- **Android Studio** (Flamingo or newer)
- **JDK 17** or newer
- **Android SDK** (API 26+)
- **Git** for version control

### Initial Setup
1. **Fork and clone** the repository:
   ```bash
   git clone https://github.com/YOUR_USERNAME/sweet.git
   cd sweet
   ```

2. **Open in Android Studio**:
   - File → Open → Select the project directory
   - Wait for Gradle sync to complete

3. **Configure Firebase**:
   - Create a Firebase project
   - Add your `google-services.json` to `app/` directory
   - Configure Authentication, Firestore, and Storage

4. **Setup Google Maps**:
   - Get a Google Maps API key
   - Update the key in `AndroidManifest.xml`

5. **Run the app**:
   ```bash
   ./gradlew clean build
   ```

### Branch Naming Convention
- `feature/your-feature-name` - New features
- `bugfix/issue-description` - Bug fixes
- `hotfix/critical-issue` - Critical fixes
- `docs/documentation-update` - Documentation changes

## 📁 Project Structure

Understanding the project architecture will help you contribute effectively:

```
app/src/main/java/com/cmu/sweet/
├── data/                   # Data layer
│   ├── local/             # Room database
│   ├── remote/            # Firebase/API integration
│   ├── mappers/           # Data transformation
│   └── repository/        # Data access abstraction
├── helpers/               # Utility classes
├── ui/                    # User interface
│   ├── components/        # Reusable UI components
│   ├── navigation/        # App navigation
│   ├── screen/           # Screen composables
│   └── theme/            # App theming
├── utils/                # Utility functions
└── view_model/           # MVVM ViewModels
```

### Key Design Patterns
- **MVVM Architecture**: ViewModels manage UI state and business logic
- **Repository Pattern**: Centralized data access
- **Single Source of Truth**: Room database with Firebase sync
- **Jetpack Compose**: Modern declarative UI framework

## 📝 Coding Standards

### Kotlin Style Guide
Follow the [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html):

1. **Naming Conventions**:
   - `PascalCase` for classes and interfaces
   - `camelCase` for functions and variables
   - `UPPER_SNAKE_CASE` for constants
   - `lowercase` for package names

2. **Code Organization**:
   ```kotlin
   // ✅ Good
   class EstablishmentViewModel(
       private val repository: EstablishmentRepository
   ) : ViewModel() {
       
       private val _establishments = MutableLiveData<List<Establishment>>()
       val establishments: LiveData<List<Establishment>> = _establishments
       
       fun loadEstablishments() {
           // Implementation
       }
   }
   ```

3. **Function Structure**:
   - Keep functions small and focused
   - Use meaningful parameter names
   - Document complex logic with comments

### Compose Best Practices
1. **State Management**:
   ```kotlin
   @Composable
   fun EstablishmentScreen(
       viewModel: EstablishmentViewModel = viewModel()
   ) {
       val establishments by viewModel.establishments.collectAsState()
       
       LazyColumn {
           items(establishments) { establishment ->
               EstablishmentCard(establishment = establishment)
           }
       }
   }
   ```

2. **Component Design**:
   - Create reusable components
   - Use preview annotations for development
   - Follow Material Design 3 guidelines

### Database Design
1. **Entity Relationships**:
   - Use proper foreign key constraints
   - Include appropriate indices
   - Document entity relationships

2. **Data Validation**:
   - Validate data at the repository level
   - Use proper data types
   - Handle null safety appropriately

## 📨 Commit Guidelines

### Commit Message Format
```
type(scope): description

[optional body]

[optional footer]
```

### Types
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, etc.)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Maintenance tasks

### Examples
```bash
feat(auth): add Google Sign-In support
fix(location): resolve geofencing permission issue
docs(readme): update installation instructions
refactor(database): optimize establishment queries
```

## 🔄 Pull Request Process

### Before Submitting
1. **Run tests** to ensure nothing is broken
2. **Update documentation** if needed
3. **Test on different devices** if possible
4. **Review your own code** for potential issues

### PR Description Template
```markdown
## Description
Brief description of changes made.

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Documentation update
- [ ] Performance improvement

## Testing
- [ ] Unit tests pass
- [ ] Manual testing completed
- [ ] Tested on different screen sizes

## Screenshots (if applicable)
Add screenshots for UI changes.

## Checklist
- [ ] Code follows style guidelines
- [ ] Self-review completed
- [ ] Documentation updated
- [ ] No new warnings introduced
```

### Review Process
1. **Automated checks** must pass
2. **Code review** by maintainers
3. **Testing** on different configurations
4. **Documentation review** if applicable

## 🧪 Testing Guidelines

### Unit Testing
```kotlin
@Test
fun `calculateDistance returns correct distance between coordinates`() {
    val distance = GeoUtils.calculateDistance(
        lat1 = 40.7128, lon1 = -74.0060,  // New York
        lat2 = 34.0522, lon2 = -118.2437  // Los Angeles
    )
    
    assertThat(distance).isGreaterThan(3900.0)
    assertThat(distance).isLessThan(4000.0)
}
```

### Compose Testing
```kotlin
@Test
fun establishmentCard_displaysCorrectInformation() {
    val establishment = Establishment(
        name = "Test Restaurant",
        address = "123 Main St",
        type = "Restaurant",
        // ... other fields
    )
    
    composeTestRule.setContent {
        EstablishmentCard(establishment = establishment)
    }
    
    composeTestRule.onNodeWithText("Test Restaurant").assertIsDisplayed()
    composeTestRule.onNodeWithText("123 Main St").assertIsDisplayed()
}
```

### Integration Testing
- Test complete user flows
- Verify database operations
- Test API integrations
- Validate location services

## 📚 Documentation

### Code Documentation
1. **KDoc for public APIs**:
   ```kotlin
   /**
    * Calculates the distance between two geographical coordinates.
    * 
    * @param lat1 Latitude of the first point
    * @param lon1 Longitude of the first point
    * @param lat2 Latitude of the second point
    * @param lon2 Longitude of the second point
    * @return Distance in meters
    */
   fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double
   ```

2. **Inline comments** for complex logic
3. **README updates** for new features
4. **Architecture documentation** for significant changes

### API Documentation
- Document Firebase integration
- Explain Google Maps usage
- Describe data models and relationships

## 🐛 Debugging Tips

### Common Issues
1. **Build Failures**:
   - Clean and rebuild: `./gradlew clean build`
   - Check dependency versions
   - Verify API keys configuration

2. **Location Issues**:
   - Check device permissions
   - Verify Google Play Services
   - Test on physical device

3. **Firebase Issues**:
   - Verify `google-services.json` configuration
   - Check Firebase project settings
   - Review security rules

### Debugging Tools
- **Android Studio Debugger** for code debugging
- **Firebase Console** for backend debugging
- **Timber logs** for runtime debugging
- **Layout Inspector** for UI debugging

## 🚀 Release Process

### Version Management
- Follow semantic versioning (MAJOR.MINOR.PATCH)
- Update version in `build.gradle.kts`
- Tag releases in Git

### Release Checklist
- [ ] All tests passing
- [ ] Documentation updated
- [ ] Version bumped
- [ ] Release notes prepared
- [ ] APK tested on multiple devices

## 💬 Getting Help

### Resources
- **GitHub Issues**: For bugs and feature requests
- **GitHub Discussions**: For questions and community support
- **Documentation**: Comprehensive README and code comments

### Contact
- Create an issue for technical problems
- Use discussions for general questions
- Email maintainers for security concerns

---

Thank you for contributing to Sweet! Your efforts help make this app better for everyone. 🙏