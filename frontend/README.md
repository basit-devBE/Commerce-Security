# Commerce Smart - React Frontend

Modern, sleek React frontend for the Commerce Smart e-commerce platform.

## Features

✨ **Modern UI/UX**
- Clean and sleek design with Tailwind CSS
- Fully responsive layout
- Smooth animations and transitions
- Intuitive navigation

🛍️ **Shopping Experience**
- Browse products with search and filters
- Product detail pages
- Shopping cart with quantity management
- Simple checkout process

👤 **User Features**
- User authentication (Login/Register)
- User profile management
- Order history and tracking
- Protected routes

🔒 **Admin Dashboard**
- Product management
- Category management
- Order management
- Inventory tracking

## Tech Stack

- **React 18** - UI library
- **React Router v6** - Routing
- **Tailwind CSS** - Styling
- **Heroicons** - Icons
- **Axios** - HTTP client
- **date-fns** - Date formatting

## Getting Started

### Prerequisites

- Node.js 14+ and npm
- Backend server running on http://localhost:8080

### Installation

1. Navigate to the frontend directory:
   ```bash
   cd frontend
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Start the development server:
   ```bash
   npm start
   ```

4. Open [http://localhost:3000](http://localhost:3000) in your browser

### Build for Production

```bash
npm run build
```

This creates an optimized production build in the `build` folder.

## Project Structure

```
frontend/
├── public/
│   └── index.html
├── src/
│   ├── components/
│   │   ├── Navbar.js
│   │   └── ProtectedRoute.js
│   ├── context/
│   │   ├── AuthContext.js
│   │   └── CartContext.js
│   ├── pages/
│   │   ├── Home.js
│   │   ├── Login.js
│   │   ├── Register.js
│   │   ├── Products.js
│   │   ├── ProductDetail.js
│   │   ├── Cart.js
│   │   ├── Checkout.js
│   │   ├── Orders.js
│   │   ├── Profile.js
│   │   └── AdminDashboard.js
│   ├── services/
│   │   └── api.js
│   ├── App.js
│   ├── index.js
│   └── index.css
├── package.json
└── tailwind.config.js
```

## API Integration

The frontend connects to the backend API at `http://localhost:8080/api`. All API calls include:

- Bearer token authentication
- Automatic token refresh
- Error handling
- Response interceptors

## Available Routes

### Public Routes
- `/` - Home page
- `/login` - Login page
- `/register` - Registration page
- `/products` - Product listing
- `/products/:id` - Product details
- `/cart` - Shopping cart

### Protected Routes (Requires Login)
- `/checkout` - Checkout page
- `/orders` - Order history
- `/profile` - User profile

### Admin Routes (Requires Admin Role)
- `/admin` - Admin dashboard

## Environment Variables

Create a `.env` file in the frontend directory (optional):

```env
REACT_APP_API_BASE_URL=http://localhost:8080/api
```

## Features in Detail

### Authentication
- JWT token-based authentication
- Tokens stored in localStorage
- Automatic logout on token expiration
- Protected routes with redirects

### Shopping Cart
- Persistent cart (localStorage)
- Add/remove items
- Update quantities
- Real-time total calculation

### Product Management
- Search functionality
- Category filtering
- Pagination
- Stock availability display

### Order Processing
- Simple checkout flow
- Order confirmation
- Order history tracking
- Status updates

### Admin Features
- Product CRUD operations
- Category management
- Order status management
- Inventory tracking

## Styling

The application uses Tailwind CSS with a custom color palette:

- Primary: Blue shades (#0ea5e9)
- Background: Gray shades
- Custom scrollbar
- Smooth transitions

## Browser Support

- Chrome (latest)
- Firefox (latest)
- Safari (latest)
- Edge (latest)

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Open a Pull Request

## License

This project is part of the Commerce Smart e-commerce platform.
