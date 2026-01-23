## Firebase & Clean Architecture in VetBook

### Layers

- **presentation**: Jetpack Compose UI + ViewModels (no Firebase imports)
- **domain**: `models`, `repository` interfaces, `usecases`
- **data**: DTOs, mappers, `Remote*DataSource` + Firebase implementations, repositories

### Data flow example (User profile)

```mermaid
flowchart LR
  ui[ProfileScreen] --> vm[ProfileViewModel]
  vm --> uc[GetUserProfileUseCase]
  uc --> repo[UserRepository]
  repo --> ds[RemoteUserDataSource]
  ds --> fs[Firestore users collection]
```

### Key types

- `UserRepository` (domain) → implemented by `FirebaseAuthUserRepository` (data)
- `RemoteUserDataSource` → implemented by `FirebaseUserDataSource`
- `RemotePetDataSource` → implemented by `FirebasePetDataSource`
- `RemoteCommunityDataSource` → implemented by `FirebaseCommunityDataSource`

### How to switch from mocks to Firebase

- Community/services/veterinarians currently use `@MockRepo` bindings in `RepositoryModule`.
- To use Firebase for community, change the DI binding or inject `@RemoteRepo CommunityRepository`.

### Example usage

- On sign-up / Google sign-in:
  - `AuthRepositoryImpl` creates/updates a `UserProfileDto` via `RemoteUserDataSource`.
- Profile loading:
  - `GetUserProfileUseCase` → `UserRepository.getCurrentUser()` + `getUserPets()` →
    Firestore via `FirebaseAuthUserRepository` and the remote data sources.


