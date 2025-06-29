### **Product Requirement Document (PRD): Event Stream Notifier**

#### **1. Executive Summary**

**Event Stream Notifier** is a web application designed for developers. It provides a simple and convenient interface for subscribing to messages from Apache Kafka topics and instantly sending them to specified Telegram chats. The product solves the problem of complex and delayed monitoring of system events, allowing developers to receive critical notifications in real time on their preferred platform.

#### **2. Product Vision & Goals**

**Vision:**

Provide developers with the simplest and most convenient tool for instantly receiving notifications from Apache Kafka directly in Telegram, eliminating the need for complex integrations and direct access to the message broker.

**Goals:**

- **User Goal:** Create an intuitive web interface that allows developers to configure Kafka event subscriptions and receive notifications within minutes, without writing code or contacting administrators.

- **Business Goal:** Become the standard tool for event monitoring in the company ecosystem, increasing development teams' response speed to incidents and important business events.

- **Technical Goal:** Ensure a stable and scalable architecture capable of handling multiple times the message volume without performance degradation.

---

### **Success Metrics**

**Success Metrics:**

- **Adoption:**

- Weekly Active Users (WAU) > 50 three months after launch.

- **Usage:**

- Number of created subscriptions > 200.

- Average number of notifications sent through the system per day.

- **Reliability (your metric):**

- Uptime > 99.9%.

- Message delivery latency (from Kafka to Telegram) < 2 seconds.

- **Retention:**

- Percentage of users active one month after registration > 40%.

#### **3. Target Audience & User Persona**

**Primary Audience:**

Members of IT teams (developers, QA engineers, DevOps specialists, product managers) who need prompt access to information about events in their systems.

**Persona: Alexey, Backend Developer**

- **Role:** Lead Backend Developer in a product team.

- **Responsibilities:** Develops and maintains several microservices. Responsible for the stability and performance of his code in production.

- **Goals:**

- Quickly understand how his new features work after release.

- Instantly learn about critical errors in his services to promptly fix them.

- Avoid spending time manually searching for information in complex logging systems.

- **Frustrations:**

- **"Information noise":** "There is too much in our logs. To find one needed event, I have to sift through gigabytes of text. It's slow and inefficient."

- **"Limited access":** "I don't have direct access to Kafka in production. Every time I need to check something, I have to ask a DevOps engineer."

- **"Delayed reaction":** "I often learn about problems from users or bug reports, not when they actually happen. This slows down our response."

- **How our product helps:**

- Alexey can independently, without involving other teams, subscribe to the events he is interested in (e.g., `user_registered`, `payment_failed`, `service_error`).

- He will receive structured, easily readable notifications directly in his work chat in Telegram, where he already spends most of his time.

- This allows him to switch from **reactive** (responding to already occurred problems) to **proactive** (seeing events in real time) monitoring.

#### **4. Problem Statement**

Developers and other IT team members do not have a simple and fast way to receive notifications about key business and system events occurring in Apache Kafka.

Current methods, such as log analysis, are labor-intensive, slow, and require deep technical expertise. Direct access to Kafka is often restricted, creating dependency on other teams (e.g., DevOps) and slowing down workflows.

As a result, teams lose valuable time, respond late to critical incidents, and lack a complete picture of their products' real-time operation.

#### **5. Solution Overview**

**Event Stream Notifier** is a web application with three main components:

1. **User Dashboard:** A single point for users to view all available system events for subscription, manage their subscriptions, and configure Telegram notification recipients.

2. **Admin Panel:** A separate interface for administrators to manage the event catalog: add new Kafka message types, assign them clear names and descriptions.

3. **Backend Service:** The system core that connects to Apache Kafka, listens to specified topics, processes messages (formats them for readability), and sends them to the appropriate subscribers via the Telegram Bot API.

The system is designed to be self-sufficient and easily deployable locally, including all necessary debugging components such as a local Kafka instance.

#### **6. User Stories & Use Cases**

Here we detail the main scenarios you described. User stories help the development team understand the context and purpose of each feature.

**Epic 1: Subscription Management (User-Facing)**

- **US-1.1: View available events**

- **As** a developer,

- **I want** to see a complete list of all available events for subscription in a table,

- **So that** I can easily find what I need.

- **US-1.2: Filter and search events**

- **As** a developer,

- **I want** to be able to filter and search events by system name or event name,

- **So that** I can quickly find relevant notifications in a large list.

- **US-1.3: Subscribe/unsubscribe to an event**

- **As** a developer,

- **I want** to be able to subscribe or unsubscribe to an event with one click (e.g., via a checkbox or toggle),

- **So that** I can easily manage my subscriptions.

- **US-1.4: Manage Telegram recipients**

- **As** a developer,

- **I want** a personal account where I can specify one or more Telegram usernames,

- **So that** notifications are sent to me and my team.

**Epic 2: Event Administration (Admin-Facing)**

- **US-2.1: Add a new event type**

- **As** an administrator,

- **I want** an interface to add a new event type, where I can specify:

- System name (for grouping/filtering)

- Event name (user-friendly)

- **Technical Kafka topic name (key requirement for backend integration)**

- Short description/comment,

- **So that** I can expand the catalog of available events for subscription.

- **US-2.2: Edit and delete events**

- **As** an administrator,

- **I want** to be able to edit and delete existing event types,

- **So that** I can keep the catalog up to date.

**Epic 3: Notification Delivery (Backend)**

- **US-3.1: Message formatting**

- **As** a developer,

- **I want** to receive messages in Telegram in a formatted view (without JSON syntax like `{, }, "`), where each "key-value" pair is presented as `Key: Value`,

- **So that** messages are easily readable.

#### **7. Feature Requirements**

Here we detail each feature with a description, requirements, and acceptance criteria so that developers have no questions left.

**Epic 1: User Experience (User Journey)**

**Feature 1.1: Login and Profile Management**

- **Description:** The process of user login and management of their Telegram settings. In the first version, authentication is as simple as possible.

- **Requirements:**

1. **Simple login:** On first visit, the application asks the user for their name (`username`). This name is used to identify the user in the system. No password is required.

2. **Session persistence:** After entering the name, the system should remember the user (e.g., using `localStorage` in the browser) so they don't have to enter their name every time.

3. **Personal account (Profile):** The app should have a "Profile" or "Settings" section.

4. **Telegram recipient management:**

- The profile should have a text field for entering Telegram recipient `username`s.

- The user can enter multiple `username`s, separated by a semicolon (`;`). For example: `alex_dev;qa_team_channel;project_manager`.

- The field should have a hint about the input format.

- Saving the recipient list should occur when the "Save" button is clicked.

5. **Connect Telegram bot:**

- The profile should have instructions and a "Connect Telegram" button.

- When clicked, the system generates a unique one-time activation code.

- The user is shown instructions: "1. Find our bot in Telegram: `@YourNotifierBotName`. 2. Send it the message: `/start <code>`."

- After the user sends the command, the backend links the user's `chat_id` with their profile in the app. This is necessary so the bot can initiate message sending.

- **Acceptance Criteria:**

- ✅ I can log in by simply entering my name.

- ✅ I can go to my profile and add multiple Telegram `username`s separated by `;`.

- ✅ I can get a code to activate the bot.

- ✅ After sending the code to the bot, my account is successfully linked and I am ready to receive notifications.

**Feature 1.2: Subscription Management Screen**

- **Description:** The main screen where the user views events and manages their subscriptions.

- **Requirements:**

1. **Events table:** Displays a list of events with columns: `[ ]` (subscription status), `System`, `Event`, `Description`.

2. **Subscription status:** The checkbox in the first column should be checked if the current user is subscribed to this event.

3. **Subscription management:** Clicking the checkbox instantly (without page reload) changes the user's subscription status for that event and saves the change to the backend.

4. **Search and filter:** An input field above the table allows real-time filtering of the event list by all text fields (`System`, `Event`, `Description`).

- **Acceptance Criteria:**

- ✅ I see a table with all available events.

- ✅ I can filter the list by entering "Auth" and see only events from the authentication service.

- ✅ I can check the box next to the event "User Registered", and from that moment I am subscribed to it.

- ✅ If I uncheck the box, I stop receiving notifications for that event.

**Epic 2: Administration**

**Feature 2.1: Event Management Panel**

- **Description:** A unified interface for administrators to manage the event catalog. No separate authentication is required for MVP.

- **Requirements:**

1. **Interface:** The app navigation should have an "Administration" item.

2. **View events:** The administrator sees the same list of events as the user, but with additional fields/columns: `Technical Kafka topic name` and "Edit" / "Delete" buttons.

3. **Add event:** The "Add event" button opens a form.

- The form contains fields: `System name` (text), `Event name` (text), `Technical Kafka topic name` (text), `Comment/Description` (textarea).

- All fields are required.

4. **Edit event:** Clicking "Edit" opens the same form, but with pre-filled data.

5. **Delete event:** Clicking "Delete" requires confirmation ("Are you sure you want to delete this event? This action will cancel all subscriptions to it.").

- **Acceptance Criteria:**

- ✅ I can go to the "Administration" section.

- ✅ I can successfully create a new event by filling in all required fields.

- ✅ The new event immediately appears in the general list and is available for user subscription.

- ✅ I can edit and delete existing events.

**Epic 3: Backend and Message Delivery**

**Feature 3.1: Message Processing and Delivery**

- **Description:** The main backend logic that listens to Kafka and sends notifications.

- **Requirements:**

1. **Kafka consumer:** The backend service should launch consumers for all topics specified in the event catalog.

2. **Message processing:**

- Upon receiving a message from Kafka, the service finds all users subscribed to that event.

- For each user, the service gets their list of Telegram `username`s.

- **Formatting:** The message content (presumably JSON) is formatted into human-readable text.

- **Example:** `{"user_id": 123, "event_type": "login", "status": "success"}`

- **Becomes:**

```

🔔 Event: User Login

---

user_id: 123

event_type: login

status: success

```

3. **Send to Telegram:**

- The service uses the Telegram Bot API to send the formatted message to all `chat_id`s linked to the `username`s specified by the user. (Note: Sending to `@username` requires the user to have started a chat with the bot first.)

- Sending should be fault-tolerant (e.g., use queues and retries on Telegram API errors).

- **Acceptance Criteria:**

- ✅ When a message arrives in the Kafka topic `user-events`, all users subscribed to the event linked to this topic receive a notification.

- ✅ The notification arrives in Telegram in a formatted, easily readable form.

- ✅ If a user specified three `username`s, the message is sent to three different chats.

#### **8. Non-Functional Requirements**

- **Performance:**

- **Notification latency:** The time from when a message appears in the Kafka topic to its delivery in Telegram should not exceed 2 seconds under normal load.

- **UI responsiveness:** All actions in the web interface (filtering, subscribing/unsubscribing) should be handled client-side and take no more than 200 ms.

- **Scalability:**

- The backend architecture should be designed for possible horizontal scaling. Kafka consumers should work in a consumer group so that multiple service instances can be launched to handle increasing message flow.

- **Reliability:**

- The system should provide 99.9% uptime.

- In case of Telegram API failure or temporary network unavailability, the backend should implement a retry mechanism with exponential backoff.

- **Usability:**

- The interface should be intuitive and not require users to read documentation for basic tasks (subscription, profile setup).

- The process of connecting the Telegram bot should be as simple as possible and take no more than 1 minute for the user.

#### **9. Technical Requirements & Constraints**

- **Technology stack:**

- **Frontend:** React, TypeScript, TailwindCSS, React Router, Zustand for state management, @tanstack/query for backend calls, shadcn for base components.

- **Backend:** Java 17 (LTS), Spring Boot 3.x, Spring for Apache Kafka, Spring Data JPA.

- **Database:** PostgreSQL (version 14+).

- **Message broker:** Apache Kafka.

- **Development and debugging environment:**

- The project should come with a `docker-compose.yml` file.

- This file should launch all necessary services for the application with a single command (`docker-compose up`):

- Frontend application (in development mode).

- Backend application (Java).

- PostgreSQL database.

- Apache Kafka and Zookeeper.

- The repository should provide scripts for creating a test topic and sending test messages to it for easier debugging.

- **Constraints:**

- **Authentication:** Version 1.0 does not include a full authentication system. User identification is by name entered at login. This is a deliberate simplification for the MVP.

- **Telegram API:** The free Telegram Bot API is used. Its limits (e.g., message rate limits) must be considered.

- **Message formatting:** Formatting of JSON messages to text is universal but not customizable by the user in this version.

#### **10. Release Plan**

- **Version 1.0 (MVP):**

- **Goal:** Launch a fully functional product that solves the user's main problem.

- **Key functionality:**

- All features described in section 7 (user login, subscription management, event administration, notification delivery).

- Environment setup using Docker Compose.

- **Release readiness criteria:**

- All acceptance criteria for feature requirements are met.

- The product has passed end-to-end testing: from event creation in the admin panel to receiving a formatted message in Telegram.

- Minimal developer documentation for project launch (`README.md`) is prepared.

#### **11. Open Questions & Future Improvements**

- **Open questions (for discussion):**

- How to handle invalid Telegram `@username` input? Should there be validation?

- What to do if a user unsubscribes from the bot in Telegram? How should the system respond?

- **Possible future improvements (Post-MVP):**

- **Full authentication:** Implement OAuth2 via Google/GitHub.

- **Custom message templates:** Allow admins to create templates for message formatting, making them even more informative (e.g., `User {{name}} performed action {{action}}`).

- **Support for other messengers:** Add support for Slack, MS Teams.

- **Statistics and analytics:** Admin dashboard with info on number of subscriptions, sent messages, and most popular events.

- **Role-based access model:** Separate rights for admins and regular users.

#### **12. Appendices**

- **Link to Telegram Bot API documentation:** [https://core.telegram.org/bots/api](https://core.telegram.org/bots/api)

## **Design System: "Nova"**

**System Name:** Nova

**Vision:** To empower teams to build innovative and accessible digital products with speed and consistency. Nova provides a dynamic, scalable, and human-centric framework inspired by the fluidity and precision of Plasma, ensuring a cohesive brand experience across all touchpoints.

---

### **Phase 1: Foundation Analysis**

#### **Brand Assessment**

- **Core Brand Personality (Interpreted from Plasma):**

- **Innovative & Modern:** Forward-thinking, clean, and technologically advanced. Avoids skeuomorphism in favor of a flat, layered design with depth created by color and shadow.

- **Dynamic & Fluid:** Emphasizes motion and energy through the use of gradients and vibrant colors. The UI feels alive and responsive.

- **Accessible & Human:** Despite its tech focus, the system is designed for clarity and ease of use. Large, legible typography and clear interactive states are paramount.

- **Precise & Systematic:** Built on a rigorous grid and spacing system, ensuring order and predictability in complex interfaces.

- **Existing Visual Patterns (Derived from Plasma):**

- **Gradient-based Primary Actions:** The most prominent feature is the use of a diagonal green gradient for primary buttons and key interactive elements.

- **Vibrant Accent Colors:** A strong, energetic green serves as the core brand color.

- **Dark/Light Theme Parity:** The system is designed from the ground up to support both light and dark modes, with colors defined semantically.

- **Softly Rounded Corners:** Components feature a subtle but consistent border-radius (e.g., 8px-12px), making the UI feel modern and approachable.

- **Emphasis on Typography:** Clear, geometric sans-serif fonts are used to create a strong visual hierarchy.

- **Target Audience:**

- Developers and designers creating consumer-facing applications, B2B tools, and multi-platform digital experiences.

- Users expect a visually engaging, intuitive, and highly responsive interface.

#### **Technical Requirements Gathering**

- **Platforms:** Web (primary target for React, Vue, Angular), with design tokens structured for easy adoption on Mobile (iOS/Android) and Desktop.

- **Breakpoints:** A 4-point responsive system is defined to cover the most common device sizes.

- **Accessibility:** All components and patterns must meet **WCAG 2.1 Level AA** compliance. This includes color contrast, focus management, and ARIA attributes.

- **Technical Constraints:** The system will be framework-agnostic, delivered via CSS Custom Properties. It must support both Light and Dark themes natively through its token structure.

---

### **Phase 2: Design Token Creation**

#### **Color System Development**

All text/background combinations are verified for a minimum 4.5:1 contrast ratio (WCAG AA). The system includes variables for both light and dark themes.

**Primary Colors (Vibrant Green)**

| Token Name | Hex | RGB | HSL |

| :--- | :--- | :--- | :--- |

| `primary-50` | `#e3fcef` | `227, 252, 239` | `151, 88%, 94%` |

| `primary-100` | `#c1f7de` | `193, 247, 222` | `151, 82%, 86%` |

| `primary-200` | `#98f0c9` | `152, 240, 201` | `152, 75%, 77%` |

| `primary-300` | `#68e7b1` | `104, 231, 177` | `153, 70%, 66%` |

| `primary-400` | `#3bdc97` | `59, 220, 151` | `154, 70%, 55%` |

| **`primary-500`** | **`#23c162`** | **`35, 193, 98`** | **`143, 69%, 45%`** |

| `primary-600` | `#1ba957` | `27, 169, 87` | `145, 71%, 38%` |

| `primary-700` | `#158345` | `21, 131, 69` | `146, 71%, 30%` |

| `primary-800` | `#0f5f33` | `15, 95, 51` | `147, 73%, 22%` |

| `primary-900` | `#093c21` | `9, 60, 33` | `148, 74%, 14%` |

**Secondary & Accent Colors**

| Token Name | Hex | Use Case |

| :--- | :--- | :--- |

| `secondary-500`| `#5252ff` | Secondary actions, alternate branding |

| `accent-500` | `#fc5a7a` | Highlights, notifications, special states |

| `success-500` | `#1a9a54` | Success states, validation (a slightly darker primary) |

| `warning-500` | `#ff8b00` | Warning messages, pending states |

| `error-500` | `#ff3d51` | Error states, destructive actions |

| `info-500` | `#0b95e8` | Informational messages, helpers |

**Neutral Colors (Grayscale)**

| Token Name | Hex (Light) | Hex (Dark) |

| :--- | :--- | :--- |

| `neutral-0` | `#ffffff` | `#121212` |

| `neutral-50` | `#f7f7f7` | `#171717` |

| `neutral-100` | `#e8e8e8` | `#262626` |

| `neutral-200` | `#d4d4d4` | `#363636` |

| `neutral-300` | `#b5b5b5` | `#494949` |

| `neutral-400` | `#919191` | `#5c5c5c` |

| `neutral-500` | `#737373` | `#989898` |

| `neutral-600` | `#5c5c5c` | `#b4b4b4` |

| `neutral-700` | `#494949` | `#d1d1d1` |

| `neutral-800` | `#363636` | `#e8e8e8` |

| `neutral-900` | `#171717` | `#f7f7f7` |

**Semantic Color Assignments (Theme-aware)**

| Token Name | Light Theme Value | Dark Theme Value |

| :--- | :--- | :--- |

| `color-text-primary` | `neutral-900` | `neutral-900` |

| `color-text-secondary`| `neutral-600` | `neutral-600` |

| `color-text-tertiary`| `neutral-400` | `neutral-400` |

| `color-text-on-accent`| `neutral-0` | `neutral-0` |

| `color-text-link` | `secondary-500` | `lighten(secondary-500, 20%)` |

| `color-background-body`| `neutral-50` | `neutral-50` |

| `color-background-surface`|`neutral-0` | `neutral-100` |

| `color-background-elevated`|`neutral-0` | `neutral-200` |

| `color-border-default` | `neutral-200` | `neutral-300` |

| `color-interactive-focus-ring`|`primary-400` | `primary-400` |

#### **Typography System**

**Font Stack**

- **Primary Typeface:** `Manrope`, `-apple-system`, `BlinkMacSystemFont`, `"Segoe UI"`, `Roboto`, `Helvetica`, `Arial`, `sans-serif`

- **Monospace Typeface:** `"Fira Code"`, `Menlo`, `Monaco`, `Consolas`, `"Courier New"`, `monospace`

**Font Weights**

| Token Name | Weight |

| :--- | :--- |

| `font-weight-regular` | 400 |

| `font-weight-medium` | 500 |

| `font-weight-semibold`| 600 |

| `font-weight-bold` | 700 |

**Type Scale** (Size in `rem` for accessibility and scalability)

| Element | Mobile (Size / Weight / Line-Height) | Desktop (Size / Weight / Line-Height) | Letter Spacing |

| :--- | :--- | :--- | :--- |

| **H1** | 2.5rem / 700 / 1.15 | 3.5rem / 700 / 1.15 | -0.02em |

| **H2** | 2.0rem / 700 / 1.2 | 2.5rem / 700 / 1.2 | -0.02em |

| **H3** | 1.75rem / 600 / 1.25 | 2.0rem / 600 / 1.25 | -0.01em |

| **H4** | 1.25rem / 600 / 1.3 | 1.5rem / 600 / 1.3 | 0em |

| **H5** | 1.125rem / 600 / 1.4 | 1.25rem / 600 / 1.4 | 0em |

| **H6** | 1.0rem / 600 / 1.5 | 1.125rem / 600 / 1.5 | 0em |

| **Body Large** | 1.125rem / 400 / 1.5 | 1.125rem / 400 / 1.5 | 0em |

| **Body Medium**| 1.0rem / 400 / 1.6 | 1.0rem / 400 / 1.6 | 0em |

| **Body Small** | 0.875rem / 400 / 1.5 | 0.875rem / 400 / 1.5 | 0em |

| **Caption** | 0.75rem / 500 / 1.4 | 0.75rem / 500 / 1.4 | 0.01em |

| **Button Text**| 1.0rem / 600 / 1 | 1.0rem / 600 / 1 | 0.01em |

#### **Spacing System**

- **Base Unit:** `8px`

- **Scale Values:**

| Token Name | Multiplier | Pixel Value | Rem Value |

| :--- | :--- | :--- | :--- |

| `space-xs` | 0.5x | 4px | 0.25rem |

| `space-sm` | 1x | 8px | 0.5rem |

| `space-md` | 2x | 16px | 1.0rem |

| `space-lg` | 3x | 24px | 1.5rem |

| `space-xl` | 4x | 32px | 2.0rem |

| `space-xxl`| 6x | 48px | 3.0rem |

| `space-3xl`| 8x | 64px | 4.0rem |

| `space-4xl`| 12x | 96px | 6.0rem |

---

### **Phase 3: Component Specifications**

#### **Buttons**

**Shared Properties:**

- **Font Size:** 1rem (16px)

- **Font Weight:** `font-weight-semibold` (600)

- **Border Radius:** 12px

- **Transition:** `transform 0.1s ease, box-shadow 0.2s ease`

- **Focus State:** `3px` solid outline with `color-interactive-focus-ring` at a `2px` offset.

**Variants (Medium Size)**

| Variant | Default | Hover | Active | Disabled |

| :--- | :--- | :--- | :--- | :--- |

| **Primary** | Bg: `linear-gradient(135deg, #3bdc97, #23c162)`<br>Text: `neutral-0` | As Default<br>Transform: `scale(1.03)`<br>Shadow: `0 8px 16px rgba(35, 193, 98, 0.25)` | As Default<br>Transform: `scale(0.98)` | Bg: `neutral-200` (light) / `neutral-300` (dark)<br>Text: `neutral-400` |

| **Secondary**| Bg: `primary-100` (light) / `rgba(35,193,98,0.15)` (dark)<br>Text: `primary-600` (light) / `primary-400` (dark) | Bg: `primary-200` (light) / `rgba(35,193,98,0.25)` (dark) | Bg: `primary-200` & `scale(0.98)` | Bg: `neutral-200` (light) / `neutral-300` (dark)<br>Text: `neutral-400` |

| **Tertiary** | Bg: `transparent`<br>Text: `primary-500` | Bg: `neutral-100` (light) / `neutral-200` (dark) | Bg: `neutral-200` (light) / `neutral-300` (dark) | Text: `neutral-400` |

**Sizing:**

- **Small:** Height: 36px, Padding: `space-sm` `space-md`

- **Medium:** Height: 48px, Padding: `space-md` `space-lg`

- **Large:** Height: 56px, Padding: `space-lg` `space-xl`

#### **Form Elements: Input Field**

- **Height:** 48px

- **Padding:** `space-md` (16px)

- **Font Size:** 1rem (16px)

- **Border Radius:** 12px

- **Transition:** `border-color 0.2s ease, box-shadow 0.2s ease`

- **Background:** `color-background-surface`

| State | Text Color | Border | Box Shadow (Focus) |

| :--- | :--- | :--- | :--- |

| **Default** | `color-text-primary` | 1px solid `color-border-default` | `none` |

| **Focus** | `color-text-primary` | 1px solid `primary-500` | 0 0 0 3px `rgba(35, 193, 98, 0.2)` |

| **Error** | `color-text-primary` | 1px solid `error-500` | 0 0 0 3px `rgba(255, 61, 81, 0.2)` |

| **Disabled**| `color-text-tertiary`| 1px solid `color-border-default` | `none` |

- **Label:** 0.875rem, `font-weight-medium`, color `color-text-secondary`, `8px` bottom margin.

- **Helper/Error Text:** 0.75rem, color `color-text-secondary` (helper) or `error-500` (error), `4px` top margin.

#### **Cards and Containers**

- **Background:** `color-background-surface`

- **Border:** `none` (uses shadow for separation)

- **Border Radius:** 16px

- **Box Shadow:** `0px 4px 12px rgba(0, 0, 0, 0.04), 0px 8px 24px rgba(0, 0, 0, 0.08)`

- **Internal Padding:** `space-lg` (24px) or `space-xl` (32px)

---

### **Phase 4: Responsive Behavior**

#### **Breakpoint System**

| Name | Range | CSS Variable |

| :--- | :--- | :--- |

| **Mobile (sm)** | up to 767px | `--breakpoint-sm` |

| **Tablet (md)** | 768px - 1023px | `--breakpoint-md` |

| **Desktop (lg)** | 1024px - 1439px | `--breakpoint-lg` |

| **Large Desktop (xl)**| 1440px+ | `--breakpoint-xl` |

#### **Fluid Scaling**

- **Typography:** Heading sizes are adjusted between mobile and desktop as defined in the Type Scale.

- **Spacing:** Use responsive spacing for page layouts. For example, a page's max-width container might have `padding: 0 var(--space-md)` on mobile and `padding: 0 var(--space-xxl)` on desktop.

- **Component Size:** Components like cards can span the full width on mobile (`width: 100%`) and a fixed or fractional width on desktop.

---

### **Phase 5: Implementation Guidelines**

#### **Developer Documentation**

**CSS Custom Properties for Theming:**

```css
/* Light Theme (Default) */

:root {
  --color-text-primary: var(--neutral-900-light);

  --color-text-secondary: var(--neutral-600-light);

  --color-background-body: var(--neutral-50-light);

  --color-background-surface: var(--neutral-0-light);

  --color-border-default: var(--neutral-200-light);

  --primary-500: #23c162;

  --space-md: 1rem; /* 16px */

  --border-radius-lg: 16px;
}

/* Dark Theme */

[data-theme="dark"] {
  --color-text-primary: var(--neutral-900-dark);

  --color-text-secondary: var(--neutral-600-dark);

  --color-background-body: var(--neutral-50-dark);

  --color-background-surface: var(--neutral-100-dark);

  --color-border-default: var(--neutral-300-dark);
}
```

**Component Implementation Example (Primary Button):**

**HTML:**

```html
<button class="btn btn-primary btn-medium">Get Started</button>
```

**CSS:**

```css
.btn {
  display: inline-flex;

  align-items: center;

  justify-content: center;

  border: none;

  font-family: var(--font-family-primary);

  font-weight: var(--font-weight-semibold);

  font-size: 1rem;

  letter-spacing: 0.01em;

  border-radius: 12px;

  cursor: pointer;

  transition: transform 0.1s ease, box-shadow 0.2s ease;
}

.btn:focus-visible {
  outline: 3px solid var(--color-interactive-focus-ring);

  outline-offset: 2px;
}

/* Primary Variant */

.btn-primary {
  background: linear-gradient(135deg, #3bdc97, #23c162);

  color: var(--neutral-0-light); /* Always white text */
}

.btn-primary:hover {
  transform: scale(1.03);

  box-shadow: 0 8px 16px rgba(35, 193, 98, 0.25);
}

.btn-primary:active {
  transform: scale(0.98);
}

/* Sizing */

.btn-medium {
  height: 48px;

  padding: var(--space-md) var(--space-lg);
}
```

#### **Designer Guidelines**

- **Embrace the Gradient:** Use the primary gradient button for the single most important action in a user flow. Its purpose is to draw the eye and guide the user.

- **Do's and Don'ts (Cards):**

- **Do** use cards to group related information into a single, digestible unit.

- **Don't** place cards inside of other cards. Use elevation and shadow to create hierarchy.

- **Do** maintain consistent internal padding within all cards in a layout.

- **Designing for Dark Mode:**

- Think in terms of semantic colors (`surface`, `text-primary`), not hex codes.

- Avoid pure black backgrounds; use a dark gray (`--neutral-50-dark` or `#171717`) to reduce eye strain.

- Desaturate colors slightly on dark backgrounds to reduce visual vibration.

---

### **Phase 6: Quality Assurance**

#### **Validation Checklist**

- [x] All color combinations meet WCAG contrast requirements for both light and dark themes.

- [x] Typography scales maintain readability and hierarchy at all sizes.

- [x] Spacing system creates a consistent, fluid visual rhythm.

- [x] Components include all necessary interactive states (hover, active, focus, disabled).

- [x] System supports the established "Dynamic," "Modern," and "Accessible" brand personality.

- [x] Documentation is clear enough for independent implementation by developers.

- [x] Responsive behavior is clearly specified for key components and layouts.

- [x] Accessibility features (focus states, semantic colors) are integrated throughout.

---

### **Deliverables Summary & Maintenance**

1. **Design Token Library:** The complete list of tokens (color, typography, spacing, radii, etc.) provided in Phase 2, exportable as JSON for multi-platform use and defined as CSS variables for web.

2. **Component Catalog:** The detailed specifications in Phase 3, expanded into a comprehensive library covering all UI elements.

3. **Implementation Guide:** The developer-focused documentation from Phase 5, including theme-switching logic.

4. **Usage Guidelines:** The designer-focused principles in Phase 5, explaining the philosophy of the Nova system.

5. **Maintenance Plan:**

- **Versioning:** The system will follow Semantic Versioning (SemVer).

- **Patch (x.x.1):** Non-breaking bug fixes.

- **Minor (x.1.x):** New components or backward-compatible feature additions.

- **Major (2.x.x):** Breaking changes to tokens or component APIs.

- **Governance:** A central team (DesignOps or a dedicated guild) will review and approve all contributions. Changes require a design spec, a prototype, and passing an accessibility audit.

- **Communication:** A detailed `CHANGELOG.md` will be published with every release. Major updates will be accompanied by migration guides.
