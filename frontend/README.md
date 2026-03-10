# HDFC Insurance Dashboard - Flutter UI

A Flutter implementation of the HDFC Bank insurance dashboard, featuring Material 3 design with custom HDFC branding.

## 🎨 Features

- **Material 3** with custom HDFC theme
- **Responsive layout** - adapts to desktop/tablet/mobile
- **Reusable widgets** - clean, modular architecture
- **Category filtering** - filter policies by type
- **Dynamic status badges** - Active (green) / Due (yellow) / expired (grey)
- **Indian currency formatting** - proper ₹ symbol and number formatting
- **Null safety** enabled

## 📁 Folder Structure

```
lib/
├── main.dart                    # App entry point
├── screens/
│   └── dashboard_screen.dart    # Main dashboard screen
├── widgets/
│   ├── custom_appbar.dart       # HDFC branded AppBar
│   ├── summary_card.dart        # Metric summary cards
│   ├── category_filter.dart     # Filter pill buttons
│   └── policy_card.dart         # Policy information cards
├── models/
│   └── policy_model.dart        # Policy data model
└── theme/
    └── app_theme.dart           # Theme configuration
```
## Key Components

### CustomAppBar
- HDFC logo with blue background
- Customer name and ID
- Avatar with initials
- Logout button

### SummaryCard
- Icon with background
- Title and value
- Optional subtitle
- Soft shadow and border

### CategoryFilter
- Pill-shaped buttons
- Active/inactive states
- Smooth selection animation

### PolicyCard
- Shield icon
- Status badge (Active/Due)
- Policy details
- Premium and sum insured
- Arrow indicator

