# DoseBuddy Registration Flow

## Mermaid Diagram Code

```mermaid
graph TD
    A[App Launch] --> B[LoginActivity]
    B --> C{User has account?}
    C -->|No| D[RegisterActivity]
    C -->|Yes| E[Enter credentials]
    D --> F[Fill registration form]
    F --> G[Validate inputs]
    G -->|Invalid| H[Show errors]
    H --> F
    G -->|Valid| I[Check username/email exists]
    I -->|Exists| J[Show error message]
    J --> F
    I -->|Available| K[Hash password]
    K --> L[Save to database]
    L --> M[Registration successful]
    M --> B
    E --> N[Validate credentials]
    N -->|Invalid| O[Show error]
    O --> E
    N -->|Valid| P[MainActivity - Dashboard]
    P --> Q[Logout] 
    Q --> B
    
    style A fill:#e1f5fe
    style P fill:#c8e6c9
    style D fill:#fff3e0
    style B fill:#f3e5f5
```

## Flow Description

1. **App Launch**: User opens DoseBuddy app
2. **LoginActivity**: Entry point for authentication
3. **Registration Path**: New users fill registration form with validation
4. **Login Path**: Existing users authenticate with credentials
5. **Dashboard**: Successful authentication leads to main app functionality
6. **Logout**: Users can return to login screen

## Key Features

- **Input Validation**: Real-time validation of all form fields
- **Security**: Password hashing with SHA-256 and salt
- **Database**: Local Room database for offline functionality
- **Error Handling**: User-friendly error messages
- **Material Design**: Modern Android UI components
