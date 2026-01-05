/**
 * Command Registry - Extensible system for registering commands
 * 
 * This allows other parts of the application to register custom commands
 * that will appear in the command palette.
 */

import { CommandAction } from '../components/CommandPalette'

class CommandRegistry {
  private commands: Map<string, CommandAction> = new Map()
  private categories: Set<string> = new Set()

  /**
   * Register a new command
   */
  register(command: CommandAction): void {
    if (this.commands.has(command.id)) {
      console.warn(`Command with id "${command.id}" already exists. Overwriting.`)
    }
    this.commands.set(command.id, command)
    this.categories.add(command.category)
  }

  /**
   * Register multiple commands at once
   */
  registerMany(commands: CommandAction[]): void {
    commands.forEach((cmd) => this.register(cmd))
  }

  /**
   * Unregister a command
   */
  unregister(id: string): boolean {
    return this.commands.delete(id)
  }

  /**
   * Get all registered commands
   */
  getAll(): CommandAction[] {
    return Array.from(this.commands.values())
  }

  /**
   * Get commands by category
   */
  getByCategory(category: string): CommandAction[] {
    return Array.from(this.commands.values()).filter(
      (cmd) => cmd.category === category
    )
  }

  /**
   * Get all categories
   */
  getCategories(): string[] {
    return Array.from(this.categories)
  }

  /**
   * Search commands by query
   */
  search(query: string): CommandAction[] {
    const lowerQuery = query.toLowerCase()
    return Array.from(this.commands.values()).filter((action) => {
      const titleMatch = action.title.toLowerCase().includes(lowerQuery)
      const descMatch = action.description?.toLowerCase().includes(lowerQuery)
      const keywordMatch = action.keywords?.some((k) =>
        k.toLowerCase().includes(lowerQuery)
      )
      const categoryMatch = action.category.toLowerCase().includes(lowerQuery)

      return titleMatch || descMatch || keywordMatch || categoryMatch
    })
  }

  /**
   * Clear all commands
   */
  clear(): void {
    this.commands.clear()
    this.categories.clear()
  }
}

// Export singleton instance
export const commandRegistry = new CommandRegistry()

// Example: Register a custom command from anywhere in the app
// commandRegistry.register({
//   id: 'custom-action',
//   title: 'Custom Action',
//   description: 'Does something custom',
//   category: 'Custom',
//   action: () => {
//     // Do something
//   },
// })



