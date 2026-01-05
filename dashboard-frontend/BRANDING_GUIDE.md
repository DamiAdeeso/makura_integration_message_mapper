# Makura Dashboard - Branding Guide

## 🎨 Brand Colors Applied

The dashboard now uses your official Makura brand colors throughout the entire UI.

### Primary Colors
- **Deep Blue** (#0B4F6C) - Primary buttons, headers, main actions
- **Medium Blue** (#1F7FBF) - Links, secondary actions, info states
- **Warm Orange** (#F47C20) - Warnings, highlights, CTAs

### Supporting Colors
- **Dark Navy** (#0A2540) - Text, shadows, depth
- **Light Blue** (#5BC0EB) - Success states, accents
- **White** (#FFFFFF) - Backgrounds, cards

### Brand Gradient
```css
background: linear-gradient(90deg, #0B4F6C 0%, #1F7FBF 50%, #F47C20 100%);
```

---

## 🖼️ Logo Placement Guide

### **WHERE TO PLACE YOUR LOGO:**

#### **1. Primary Logo Location - Sidebar (Main Navigation)**
📍 **File:** `dashboard-frontend/src/components/MainLayout.tsx` (Line 88-93)

**Current Code:**
```tsx
<div className="logo">
  {/* TODO: Replace with your actual logo image */}
  {/* <img src="/logo.png" alt="Makura" className="logo-image" /> */}
  <ApiOutlined style={{ fontSize: 28 }} />
  {!collapsed && <span className="logo-text">MAKURA</span>}
</div>
```

**To Add Your Logo:**
1. Place your logo file in: `dashboard-frontend/public/logo.png`
2. Uncomment the `<img>` tag
3. The logo will appear in the sidebar with gradient background

**Recommended Logo Specs:**
- Format: PNG (with transparent background) or SVG
- Dimensions: 150px × 40px (or similar aspect ratio)
- Color: White or light colored (sits on gradient background)

---

#### **2. Login Page Logo**
📍 **File:** `dashboard-frontend/src/pages/LoginPage.tsx` (Line 35-37)

**Current Code:**
```tsx
<div className="login-header">
  {/* TODO: Replace with your actual logo */}
  {/* <img src="/logo.png" alt="Makura" className="login-logo" /> */}
  <ApiOutlined className="login-icon" />
  <Title level={2}>MAKURA</Title>
```

**To Add Your Logo:**
1. Use the same `dashboard-frontend/public/logo.png`
2. Uncomment the `<img>` tag
3. Logo will appear centered above the login form

**Recommended Logo Specs:**
- Format: PNG or SVG
- Max Width: 200px
- Color: Full color or dark version (sits on white card)

---

#### **3. Favicon (Browser Tab)**
📍 **File:** `dashboard-frontend/public/vite.svg` → Replace this file

**Current:** Generic Vite icon

**To Add Your Favicon:**
1. Create a favicon (16×16, 32×32, 64×64 px)
2. Replace: `dashboard-frontend/public/vite.svg` 
3. Or add: `dashboard-frontend/public/favicon.ico`
4. Update `dashboard-frontend/index.html` line 5:
   ```html
   <link rel="icon" type="image/png" href="/favicon.png" />
   ```

---

## 📁 File Structure for Logo Assets

```
dashboard-frontend/
├── public/
│   ├── logo.png              # Main logo (white/light for sidebar)
│   ├── logo-dark.png         # Optional: Dark version for light backgrounds
│   ├── favicon.ico           # Browser tab icon
│   └── logo-full.svg         # Optional: Full resolution SVG
```

---

## 🎨 Where Brand Colors Are Applied

### **1. Ant Design Theme** (`App.tsx`)
- Primary buttons: Deep Blue (#0B4F6C)
- Info elements: Medium Blue (#1F7FBF)
- Success states: Light Blue (#5BC0EB)
- Warning states: Warm Orange (#F47C20)
- Links: Medium Blue (#1F7FBF)

### **2. Sidebar Navigation** (`MainLayout.tsx/.css`)
- Logo background: Full gradient
- Dark sidebar with gradient logo area
- Active menu items: Accent colors

### **3. Login Page** (`LoginPage.tsx/.css`)
- Full page gradient background
- Gradient: Deep Blue → Medium Blue → Warm Orange
- Subtle pattern overlay
- Logo area with brand colors

### **4. Dashboard Welcome Card** (`DashboardPage.tsx/.css`)
- Full gradient background
- White text on gradient
- Prominent hero section

### **5. All Links** (`App.css`)
- Default: Medium Blue (#1F7FBF)
- Hover: Warm Orange (#F47C20)

### **6. User Avatar** (`MainLayout.tsx`)
- Gradient background (Deep Blue → Medium Blue)

---

## 🚀 Quick Setup Instructions

### Step 1: Prepare Your Logo
1. Export logo in PNG format (transparent background)
2. White or light version for sidebar
3. Full color for login page
4. Size: approximately 150px wide

### Step 2: Add Logo Files
```bash
# Navigate to public folder
cd dashboard-frontend/public

# Copy your logo files
# logo.png (for sidebar)
# favicon.ico (for browser tab)
```

### Step 3: Update Code
1. Open `dashboard-frontend/src/components/MainLayout.tsx`
2. Line 89: Uncomment the `<img>` tag
3. Open `dashboard-frontend/src/pages/LoginPage.tsx`
4. Line 36: Uncomment the `<img>` tag

### Step 4: Test
```bash
cd dashboard-frontend
npm run dev
```

Visit http://localhost:5173 and check:
- ✅ Logo appears in sidebar
- ✅ Logo appears on login page
- ✅ Favicon shows in browser tab
- ✅ Colors match brand guidelines

---

## 🎨 Color Usage Guidelines

### Primary Actions
Use **Deep Blue (#0B4F6C)** for:
- Primary buttons
- Main CTAs
- Important headers
- Active states

### Links & Navigation
Use **Medium Blue (#1F7FBF)** for:
- Text links
- Navigation items
- Info badges
- Secondary actions

### Highlights & Warnings
Use **Warm Orange (#F47C20)** for:
- Warning states
- Highlights
- Hover states
- Accent elements

### Success States
Use **Light Blue (#5BC0EB)** for:
- Success messages
- Positive indicators
- Completed states

---

## 📱 Responsive Behavior

### Desktop (> 992px)
- Sidebar expanded with full logo and text
- Gradient clearly visible
- Full color experience

### Tablet (768px - 992px)
- Sidebar collapsed by default
- Logo icon only (text hidden)
- Maintains gradient background

### Mobile (< 768px)
- Sidebar hidden
- Logo visible when menu opened
- Optimized touch targets

---

## 🔧 Customization

All brand colors are centralized in:
📍 `dashboard-frontend/src/theme/colors.ts`

To modify colors globally, update this file:
```typescript
export const COLORS = {
  deepBlue: '#0B4F6C',      // Change primary
  mediumBlue: '#1F7FBF',    // Change secondary
  warmOrange: '#F47C20',    // Change accent
  // ... more colors
}
```

---

## ✅ Checklist

- [ ] Logo prepared (PNG, ~150px wide, transparent background)
- [ ] Logo placed in `public/logo.png`
- [ ] Favicon prepared and placed in `public/favicon.ico`
- [ ] Sidebar logo uncommented in `MainLayout.tsx`
- [ ] Login logo uncommented in `LoginPage.tsx`
- [ ] Favicon link updated in `index.html`
- [ ] Tested in browser (http://localhost:5173)
- [ ] Verified responsive behavior (mobile, tablet, desktop)
- [ ] Checked all pages for consistent branding

---

## 🎉 Result

Your dashboard now features:
✅ Consistent brand colors throughout
✅ Professional gradient backgrounds
✅ Logo placement in sidebar and login
✅ Responsive design maintained
✅ Enterprise-grade appearance
✅ Full Makura brand identity

**The UI is now fully aligned with your brand!** 🚀



