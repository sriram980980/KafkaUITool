## UI Layout - Left Panel Structure

```
┌─ Left Panel (VBox) ──────────────────────────┐
│                                               │
│ ┌─ Clusters Section (TitledPane) ──────────┐ │
│ │  - Clusters ListView                     │ │
│ │  - [Add] [Edit] [Delete] [Connect]       │ │
│ └──────────────────────────────────────────┘ │
│                                               │
│ ┌─ Topics Section (TitledPane) ────────────┐ │
│ │  - Filter TextField                      │ │
│ │  - Topics ListView                       │ │
│ │  - [Create] [Config] [Delete]            │ │
│ └──────────────────────────────────────────┘ │
│                                               │
│ ┌─ Chat Section (TitledPane) - NEW ────────┐ │
│ │  ┌─ Chat Messages (TextArea) ──────────┐ │ │
│ │  │ [12:34:56] System: Connected...     │ │ │
│ │  │ [12:35:01] You: Hello!              │ │ │
│ │  │ [12:35:02] System: Message received │ │ │
│ │  └──────────────────────────────────────┘ │ │
│ │  ┌─ Input Row (HBox) ──────────────────┐ │ │
│ │  │ [Type message...] [Send]             │ │ │
│ │  └──────────────────────────────────────┘ │ │
│ │  ┌─ Control Row (HBox) ────────────────┐ │ │
│ │  │              [Clear] [Connect]       │ │ │
│ │  └──────────────────────────────────────┘ │ │
│ └──────────────────────────────────────────┘ │
└───────────────────────────────────────────────┘
```

## Chat Section Features

### Chat Messages Area
- Read-only TextArea showing conversation history
- Monospace font for consistent formatting
- Timestamps for each message
- Auto-scrolling to latest messages
- System messages for connection status

### Input Controls
- Text field for typing messages
- Send button to submit messages
- Enter key support for quick sending
- Clear button to reset chat history
- Connect/Disconnect toggle button

### Styling
- Consistent with dark theme
- Proper border and background colors
- Hover and focus states
- Button styling matches existing UI