# ISO 20022 Dashboard Frontend

Enterprise-grade React frontend for managing ISO 20022 message translation services.

## 🚀 Features

### ✅ Complete Implementation

- **Authentication & Authorization**
  - JWT-based authentication
  - Role-based access control (RBAC)
  - Permission-based UI controls
  - Secure route protection

- **Routes Management**
  - Create, read, update, delete routes
  - Toggle route activation
  - Support for ACTIVE/PASSIVE modes
  - Inbound/Outbound format configuration
  - Encryption settings (AES, PGP)
  - Search and filtering

- **Field Mappings**
  - Visual mapping builder
  - Drag-and-drop field selection
  - Multiple transformation types (DIRECT, CONCATENATE, SPLIT, FORMAT, CUSTOM)
  - JSONPath source field mapping
  - ISO 20022 target field mapping
  - Batch mapping creation

- **User Management**
  - Create and manage users
  - Multi-role assignment
  - Permission viewing
  - User activation/deactivation
  - Password management

- **Role Management**
  - Create custom roles
  - Fine-grained permission assignment
  - Quick permission templates
  - System role protection
  - User count tracking

- **Metrics & Analytics**
  - Real-time system health monitoring
  - Performance charts (Line, Bar, Pie)
  - Request status distribution
  - Top routes by volume
  - Response time tracking
  - Auto-refresh capabilities

- **UI/UX Features**
  - Responsive design (mobile, tablet, desktop)
  - Professional Ant Design components
  - Error boundaries for fault tolerance
  - Toast notifications
  - Loading states
  - Empty states
  - Confirmation dialogs
  - Tooltips and help text

## 🛠️ Tech Stack

- **React 18** - UI library
- **TypeScript** - Type safety
- **Vite** - Build tool and dev server
- **Ant Design 5** - UI component library
- **React Router 6** - Client-side routing
- **TanStack React Query** - Data fetching and caching
- **Zustand** - State management
- **Axios** - HTTP client
- **Recharts** - Data visualization
- **Monaco Editor** - Code editor (for YAML)

## 📦 Installation

```bash
# Navigate to frontend directory
cd dashboard-frontend

# Install dependencies
npm install

# Start development server
npm run dev
```

The app will run on **http://localhost:5173**

## 🔧 Configuration

### Environment Variables

Create a `.env` file:

```env
VITE_API_BASE_URL=http://localhost:8080/api
```

### Backend Integration

Ensure the backend is running on port 8080:

```bash
cd ../dashboard-backend
mvn spring-boot:run
```

## 🎯 Default Credentials

```
Username: admin
Password: admin123
```

## 📂 Project Structure

```
dashboard-frontend/
├── src/
│   ├── api/              # API client modules
│   │   ├── authApi.ts
│   │   ├── routesApi.ts
│   │   ├── usersApi.ts
│   │   ├── rolesApi.ts
│   │   ├── mappingsApi.ts
│   │   └── metricsApi.ts
│   ├── components/       # Reusable components
│   │   ├── ErrorBoundary.tsx
│   │   ├── AuthProvider.tsx
│   │   ├── MainLayout.tsx
│   │   ├── PageHeader.tsx
│   │   ├── PermissionGuard.tsx
│   │   ├── routes/       # Route-specific components
│   │   ├── users/        # User-specific components
│   │   ├── roles/        # Role-specific components
│   │   └── mappings/     # Mapping-specific components
│   ├── pages/            # Page components
│   │   ├── LoginPage.tsx
│   │   ├── DashboardPage.tsx
│   │   ├── RoutesPage.tsx
│   │   ├── UsersPage.tsx
│   │   ├── RolesPage.tsx
│   │   ├── MappingsPage.tsx
│   │   └── MetricsPage.tsx
│   ├── store/            # State management
│   │   └── authStore.ts
│   ├── App.tsx           # Root component
│   └── main.tsx          # Entry point
├── package.json
├── vite.config.ts
└── tsconfig.json
```

## 🔐 Permission System

The app uses fine-grained permissions:

### Permission Format: `resource:action`

**Routes:**
- `routes:create` - Create new routes
- `routes:view` - View routes
- `routes:update` - Update routes
- `routes:delete` - Delete routes
- `routes:toggle` - Toggle route status
- `routes:publish` - Publish routes

**Users:**
- `users:create` - Create users
- `users:view` - View users
- `users:update` - Update users
- `users:delete` - Delete users

**Roles:**
- `roles:create` - Create roles
- `roles:view` - View roles
- `roles:update` - Update roles
- `roles:delete` - Delete roles

**Mappings:**
- `mappings:create` - Create mappings
- `mappings:view` - View mappings
- `mappings:update` - Update mappings
- `mappings:delete` - Delete mappings

**Metrics:**
- `metrics:view` - View metrics
- `metrics:export` - Export metrics

**System:**
- `system:configure` - System configuration
- `system:health` - Health checks

## 🎨 UI Components

### Custom Components

1. **PageHeader** - Consistent page headers with breadcrumbs
2. **PermissionGuard** - Conditional rendering based on permissions
3. **ErrorBoundary** - Catch and display React errors
4. **AuthProvider** - Authentication state management
5. **MainLayout** - App layout with sidebar and header

### Modal Components

- CreateRouteModal / EditRouteModal
- CreateUserModal / EditUserModal
- CreateRoleModal / EditRoleModal
- CreateMappingModal / EditMappingModal
- VisualMappingBuilder

## 📊 Key Features

### Visual Mapping Builder

Interactive tool for creating field mappings:
- Tree view of source JSON structure
- List view of ISO 20022 target fields
- Transformation type selection
- Real-time mapping rule preview
- Batch save functionality

### Metrics Dashboard

Real-time monitoring:
- System health status
- Total requests counter
- Success rate calculation
- Average response time
- Request status pie chart
- Top routes bar chart
- Performance line chart over time
- Detailed route performance table

### Responsive Design

- Mobile-first approach
- Collapsible sidebar on mobile
- Adaptive table layouts
- Touch-friendly controls
- Optimized for all screen sizes

## 🚀 Build & Deploy

```bash
# Build for production
npm run build

# Preview production build
npm run preview
```

The build output will be in the `dist/` directory.

### Deploy Options

- **Static Hosting**: Netlify, Vercel, GitHub Pages
- **Docker**: Build container with Nginx
- **CDN**: Upload `dist/` to any CDN

## 🧪 Development

```bash
# Run development server with hot reload
npm run dev

# Run linter
npm run lint

# Type check
npm run type-check
```

## 🔄 API Integration

All API calls go through centralized API client modules:

```typescript
// Example: Create a new route
import { routesApi } from './api/routesApi'

const newRoute = await routesApi.create({
  routeId: 'SYSTEM_TO_NIP',
  name: 'System to NIP',
  mode: 'PASSIVE',
  inboundFormat: 'JSON',
  outboundFormat: 'JSON',
  active: true,
})
```

React Query handles:
- Automatic caching
- Background refetching
- Optimistic updates
- Error handling
- Loading states

## 📝 Contributing

1. Follow TypeScript best practices
2. Use Ant Design components
3. Implement proper error handling
4. Add loading states
5. Write clean, readable code
6. Test on multiple screen sizes

## 📄 License

Copyright © 2025 Makura Systems

---

**Built with ❤️ using React + TypeScript + Ant Design**
