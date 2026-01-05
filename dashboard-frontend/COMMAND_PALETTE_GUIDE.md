# Command Palette Guide

## Overview

The Command Palette is an AWS-style search interface that allows users to quickly navigate, perform actions, and access features using keyboard shortcuts. It's fully extensible, allowing you to add custom commands from anywhere in the application.

## Usage

### Opening the Command Palette

- **Keyboard Shortcut**: `Cmd+K` (Mac) or `Ctrl+K` (Windows/Linux)
- **Click**: Click the "Search..." button in the header
- **Close**: Press `Esc` or click outside the modal

### Navigation

- **Arrow Keys**: Navigate up/down through results
- **Enter**: Select the highlighted command
- **Escape**: Close the palette

## Adding Custom Commands

### Basic Example

```typescript
import { commandRegistry } from '../utils/commandRegistry'
import { ThunderboltOutlined } from '@ant-design/icons'

// Register a new command
commandRegistry.register({
  id: 'export-data',
  title: 'Export Data',
  description: 'Export all routes to CSV',
  icon: <ThunderboltOutlined />,
  category: 'Actions',
  keywords: ['export', 'csv', 'download', 'data'],
  action: () => {
    // Your action logic here
    console.log('Exporting data...')
  },
  shortcut: 'E D', // Optional keyboard shortcut hint
  badge: 'New', // Optional badge
})
```

### Advanced Example with Navigation

```typescript
import { commandRegistry } from '../utils/commandRegistry'
import { useNavigate } from 'react-router-dom'

// In your component
const navigate = useNavigate()

commandRegistry.register({
  id: 'view-reports',
  title: 'View Reports',
  description: 'Open the reports page',
  icon: <FileTextOutlined />,
  category: 'Navigation',
  keywords: ['reports', 'analytics', 'view'],
  action: () => {
    navigate('/reports')
  },
  shortcut: 'G R',
})
```

### Registering Multiple Commands

```typescript
import { commandRegistry } from '../utils/commandRegistry'

commandRegistry.registerMany([
  {
    id: 'action-1',
    title: 'Action 1',
    category: 'Custom',
    action: () => console.log('Action 1'),
  },
  {
    id: 'action-2',
    title: 'Action 2',
    category: 'Custom',
    action: () => console.log('Action 2'),
  },
])
```

## Command Properties

| Property | Type | Required | Description |
|----------|------|----------|-------------|
| `id` | string | Yes | Unique identifier for the command |
| `title` | string | Yes | Display name of the command |
| `description` | string | No | Helpful description shown below title |
| `icon` | ReactNode | No | Icon to display next to the command |
| `category` | string | Yes | Category for grouping (e.g., "Navigation", "Actions") |
| `keywords` | string[] | No | Additional search keywords |
| `action` | function | Yes | Function to execute when command is selected |
| `shortcut` | string | No | Keyboard shortcut hint (e.g., "G D" for Go Dashboard) |
| `badge` | string | No | Optional badge text (e.g., "New", "Beta") |

## Integration with Page Components

### Triggering Create Modals

Commands can trigger custom events that pages can listen to:

```typescript
// In your command
action: () => {
  navigate('/routes')
  setTimeout(() => {
    const event = new CustomEvent('command:create-route')
    window.dispatchEvent(event)
  }, 100)
}

// In your RoutesPage component
useEffect(() => {
  const handleCreateRoute = () => {
    setCreateModalVisible(true)
  }
  
  window.addEventListener('command:create-route', handleCreateRoute)
  return () => {
    window.removeEventListener('command:create-route', handleCreateRoute)
  }
}, [])
```

## Best Practices

1. **Use Descriptive Keywords**: Add multiple keywords to make commands easy to find
2. **Categorize Properly**: Use consistent category names
3. **Provide Descriptions**: Help users understand what the command does
4. **Use Icons**: Visual icons make commands easier to scan
5. **Register Early**: Register commands in component mount or initialization
6. **Clean Up**: Unregister commands when components unmount if needed

## Example: Adding a Feature Command

```typescript
// In your feature component (e.g., ReportsPage.tsx)
import { useEffect } from 'react'
import { commandRegistry } from '../utils/commandRegistry'
import { FileTextOutlined } from '@ant-design/icons'

export const ReportsPage = () => {
  useEffect(() => {
    // Register command when component mounts
    commandRegistry.register({
      id: 'generate-report',
      title: 'Generate Report',
      description: 'Create a new analytics report',
      icon: <FileTextOutlined />,
      category: 'Reports',
      keywords: ['report', 'generate', 'create', 'analytics'],
      action: () => {
        // Open report generation modal
        setGenerateModalVisible(true)
      },
      shortcut: 'G R',
    })

    // Optional: Clean up on unmount
    return () => {
      commandRegistry.unregister('generate-report')
    }
  }, [])

  // ... rest of component
}
```

## Keyboard Shortcuts Reference

### Global Shortcuts
- `Cmd/Ctrl + K`: Open command palette
- `Esc`: Close command palette

### Navigation Shortcuts (in palette)
- `G D`: Go to Dashboard
- `G R`: Go to Routes
- `G M`: Go to Mappings
- `G T`: Go to Metrics
- `G U`: Go to Users
- `G P`: Go to Roles & Permissions
- `G S`: Go to Settings

### Action Shortcuts (in palette)
- `C R`: Create Route
- `C U`: Create User
- `C M`: Create Mapping

## Extending for Future Features

The command registry is designed to be extensible. As you add new features:

1. **Create Feature-Specific Commands**: Each major feature can register its own commands
2. **Use Custom Events**: Commands can dispatch custom events for complex actions
3. **Dynamic Registration**: Commands can be registered/unregistered based on user permissions
4. **Context-Aware Commands**: Commands can check current page/state before executing

## Troubleshooting

### Command Not Appearing
- Check that the command is registered before the palette opens
- Verify the `id` is unique
- Ensure the search query matches title, description, or keywords

### Action Not Executing
- Check browser console for errors
- Verify the action function is properly defined
- Ensure navigation/routing is set up correctly

### Keyboard Shortcut Not Working
- Check for conflicts with browser shortcuts
- Verify the shortcut handler is properly attached
- Test in different browsers



