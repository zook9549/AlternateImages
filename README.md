# Alternate Images
 Displays ChatGPT generated images in a configured Tidbyt.
## Setup
Configure accounts for ChatGPT and Tidbyt. Ideally, configurations should be added to a profile-specific `application.properties`, which can be easily managed through `application-default.properties`.
### 1. ChatGPT
- Purpose: Generates images from prompt.

Configuration Key:
```markdown
chatgpt.api.key=YOUR_CHATGPT_API_KEY
```

### 2. Tidbyt
- Purpose: Displays image from prompt.  This are obtained from within the mobile app settings for Tidbyt.

Configuration Key:
```markdown
tidbyt.api.key=YOUR_TIDBYT_API_KEY
tidbyt.device.id=YOUR_TIDBYT_DEVICE_ID
```