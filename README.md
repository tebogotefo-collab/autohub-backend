# AutoParts Hub - Car Parts Marketplace

AutoParts Hub is a comprehensive online platform connecting buyers seeking specific automotive parts with a diverse range of sellers. The platform facilitates easy discovery of compatible parts, secure transactions, and reliable fulfillment, catering to the needs of the South African automotive aftermarket.

## Features

- **User Authentication**: Secure registration and login for buyers and sellers
- **Vehicle Compatibility**: Find parts compatible with specific vehicles
- **Seller Dashboard**: Manage listings, orders, and inventory
- **Advanced Search**: Find parts by category, brand, condition, or vehicle compatibility
- **Image Management**: Upload and manage multiple images for listings
- **User Profiles**: Manage user and seller profiles

## Technology Stack

- **Backend**: Spring Boot 3.1 with Java 17
- **Database**: PostgreSQL
- **Authentication**: JWT (JSON Web Tokens)
- **ORM**: Hibernate/JPA
- **Migration**: Flyway
- **Cache**: Redis
- **Search**: Elasticsearch (for advanced search capabilities)
- **Frontend** (planned): React for web, React Native for mobile

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL 14+
- Redis (optional, for caching)
- Elasticsearch (optional, for advanced search)

### Setup Database

1. Create a PostgreSQL database:
```sql
CREATE DATABASE autopartshub;
CREATE USER autopartshub_user WITH ENCRYPTED PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE autopartshub TO autopartshub_user;
```

2. Update the database credentials in `src/main/resources/application.yml`

### Running the Application

1. Clone the repository
```bash
git clone https://github.com/MathoTech/autoparts-hub.git
cd autoparts-hub
```

2. Build the project
```bash
mvn clean install
```

3. Run the application
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080` with API endpoints available at `http://localhost:8080/api`.

### API Documentation

API documentation is available via Swagger UI at `http://localhost:8080/api/swagger-ui.html` when the application is running.

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register a new user
- `POST /api/auth/login` - Login and get JWT token

### User Profile
- `GET /api/users/profile` - Get current user profile
- `PUT /api/users/profile` - Update current user profile

### Categories
- `GET /api/categories` - Get all parent categories
- `GET /api/categories/{id}` - Get category by ID
- `GET /api/categories/{id}/subcategories` - Get subcategories

### Brands
- `GET /api/brands` - Get all brands
- `GET /api/brands/{id}` - Get brand by ID

### Listings
- `GET /api/listings` - Get all listings (paginated)
- `GET /api/listings/{id}` - Get listing details
- `POST /api/listings` - Create a new listing
- `PUT /api/listings/{id}` - Update a listing
- `DELETE /api/listings/{id}` - Delete a listing
- `GET /api/listings/category/{categoryId}` - Get listings by category
- `GET /api/listings/filter` - Filter listings by various criteria
- `GET /api/listings/vehicle-compatibility` - Get listings compatible with a vehicle

### Images
- `GET /api/listings/{listingId}/images` - Get images for a listing
- `POST /api/listings/{listingId}/images` - Upload a new image for a listing
- `DELETE /api/images/{imageId}` - Delete an image
- `PATCH /api/images/{imageId}/set-primary` - Set an image as primary

## Project Structure

```
autoparts-hub/
├── src/
│   ├── main/
│   │   ├── java/com/mathotech/autopartshub/
│   │   │   ├── config/          # Configuration classes
│   │   │   ├── controller/      # REST controllers
│   │   │   ├── dto/             # Data transfer objects
│   │   │   ├── exception/       # Exception handling
│   │   │   ├── model/           # JPA entities
│   │   │   ├── repository/      # Spring Data repositories
│   │   │   ├── security/        # Security configuration
│   │   │   ├── service/         # Business logic
│   │   │   └── util/            # Utility classes
│   │   └── resources/
│   │       ├── db/migration/    # Flyway migrations
│   │       └── application.yml  # Application configuration
│   └── test/                    # Test classes
└── pom.xml                      # Maven build file
```

## Development Roadmap

- **Phase 1**: Core Backend & Database Setup ✅
- **Phase 2**: User Authentication & Product Listing ✅
- **Phase 3**: Vehicle Compatibility & Search ✅
- **Phase 4**: Order Management & Payment Integration
- **Phase 5**: Notifications & Messaging
- **Phase 6**: Admin Dashboard
- **Phase 7**: Mobile Application (React Native)
- **Phase 8**: Analytics & Reporting

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgements

- MathoTech Team
- South African Automotive Aftermarket
