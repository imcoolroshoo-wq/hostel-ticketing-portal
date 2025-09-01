# IIM Trichy Hostel Ticket Management System
## Product Design Document

---

## **1. EXECUTIVE SUMMARY**

### **1.1 Product Vision**
To create a comprehensive, efficient, and user-friendly ticket management system that streamlines the resolution of hostel infrastructure and IT issues at IIM Trichy, ensuring optimal living conditions for students and efficient resource utilization for the administration.

### **1.2 Product Mission**
Provide a centralized platform that enables students to report issues seamlessly, allows staff to manage and resolve problems efficiently, and gives administrators complete visibility and control over hostel operations.

### **1.3 Success Metrics**
- **Issue Resolution Time**: Reduce average resolution time by 60%
- **Student Satisfaction**: Achieve 90%+ satisfaction rating
- **Operational Efficiency**: Increase staff productivity by 40%
- **System Adoption**: 95%+ user adoption within 3 months
- **Issue Prevention**: 30% reduction in recurring issues

---

## **2. PROBLEM STATEMENT**

### **2.1 Current Challenges**
- **Manual Process**: Students report issues through phone calls, emails, or in-person visits
- **No Tracking**: Lack of visibility into issue status and resolution progress
- **Inefficient Assignment**: Issues assigned manually without considering staff expertise or workload
- **No Accountability**: Difficult to track responsibility and performance
- **Data Loss**: No historical data for trend analysis or preventive maintenance
- **Communication Gaps**: Poor communication between students, staff, and administration

### **2.2 Impact Analysis**
- **Student Experience**: Frustration due to unresolved issues and lack of transparency
- **Staff Efficiency**: Time wasted on manual coordination and duplicate work
- **Administrative Oversight**: Limited visibility into operational performance
- **Resource Utilization**: Suboptimal allocation of maintenance resources
- **Cost Implications**: Higher operational costs due to inefficiencies

---

## **3. TARGET USERS & PERSONAS**

### **3.1 Primary Users**

#### **Student (Reporter)**
- **Profile**: MBA students residing in hostel
- **Goals**: Quick issue reporting, real-time status updates, transparent communication
- **Pain Points**: Long resolution times, lack of status visibility, multiple follow-ups
- **Usage Pattern**: Occasional use when issues arise, mobile-first preference
- **Access Rights**: 
  - View only their own tickets
  - Create new tickets
  - Reopen/close their own tickets
  - Provide feedback and ratings

#### **Maintenance Staff (Resolver)**
- **Profile**: Specialized technicians (electrical, plumbing, housekeeping, IT)
- **Goals**: Clear work assignments, efficient task management, performance tracking
- **Pain Points**: Unclear priorities, manual coordination, lack of work history
- **Usage Pattern**: Daily use during work hours, desktop/tablet preference
- **Access Rights**:
  - View only tickets assigned to them
  - Update status of assigned tickets
  - Add comments and work logs
  - Request reassignment if needed

#### **Hostel Administration (Manager)**
- **Profile**: Senior administrators overseeing entire hostel operations
- **Goals**: Complete system control, strategic oversight, performance analytics, resource optimization
- **Pain Points**: Need for comprehensive control and visibility
- **Usage Pattern**: Daily system management, weekly/monthly reviews, dashboard monitoring
- **Access Rights**:
  - **Complete Ticket Control**: Create, view, edit, delete, assign, reassign, status updates
  - **User Management**: Create, modify, deactivate user accounts
  - **Mapping Management**: Create/update/remove staff-hostel-category mappings
  - **System Configuration**: All administrative functions
  - **Analytics Access**: Complete reporting and analytics

### **3.2 Secondary Users**

#### **Security Staff**
- **Profile**: 24/7 security personnel
- **Goals**: Handle emergency issues, coordinate with maintenance staff
- **Usage Pattern**: Emergency situations, night shift coordination

#### **Housekeeping Supervisors**
- **Profile**: Cleaning and maintenance supervisors
- **Goals**: Schedule routine maintenance, manage housekeeping staff
- **Usage Pattern**: Daily task assignment and monitoring

---

## **4. FUNCTIONAL REQUIREMENTS**

### **4.1 Core Features**

#### **4.1.1 Issue Reporting**
- **Quick Report**: Simple form with essential fields (title, description, location, priority)
- **Category Selection**: Predefined categories with option for custom categories
- **Location Mapping**: Building and room number selection with auto-complete
- **Priority Setting**: System-suggested priority with user override option
- **Photo Attachment**: Support for image uploads to illustrate issues
- **Duplicate Detection**: Alert users about similar existing issues

#### **4.1.2 Ticket Management**
- **Unique Identification**: Auto-generated ticket numbers with meaningful format
- **Status Tracking**: 8-stage workflow (Open → Assigned → In Progress → On Hold → Resolved → Closed → Cancelled → Reopened)
- **Assignment Logic**: Automated assignment with manual override capability
- **Priority Management**: Dynamic priority adjustment based on urgency and impact
- **Time Tracking**: Automatic logging of time spent in each status
- **Communication Thread**: Internal comments and updates

#### **4.1.3 Assignment & Routing**
- **Multi-Staff Mapping**: Multiple staff members can be mapped to same hostel-category combination
- **Algorithmic Assignment**: Automatic assignment based on staff bandwidth and current workload
- **Mapping Management**: Admin-controlled staff-hostel-category mapping system
- **Workload Intelligence**: Real-time workload analysis for optimal assignment
- **Manual Override**: Admin can assign/reassign tickets to any staff member
- **Custom Category Handling**: Custom categories require manual admin assignment
- **Escalation Rules**: Automatic escalation based on time thresholds and priority
- **Fallback Assignment**: Default assignment when no specific mapping exists

#### **4.1.4 Workflow Management**
- **Status Transitions**: Controlled progression through defined workflow stages
- **Approval Workflows**: Multi-level approval for high-cost or complex issues
- **SLA Management**: Service level agreements with automatic alerts for breaches
- **Bulk Operations**: Mass updates for similar issues
- **Recurring Issues**: Template creation for common problems

### **4.2 Ticket Categories & Resolution Framework**

#### **4.2.1 Infrastructure Categories**

##### **Electrical Issues**
- **Description**: Power-related problems, electrical fixtures, and safety concerns
- **Examples**: Power outages, faulty outlets, lighting issues, electrical appliances, circuit breakers
- **Expected Resolution**: 2-4 hours (Emergency), 4-8 hours (High), 1-2 days (Medium/Low)
- **Required Skills**: Certified electrician, electrical safety knowledge
- **Common Tools**: Multimeter, electrical tools, safety equipment

##### **Plumbing & Water**
- **Description**: Water supply, drainage, and sanitation issues
- **Examples**: Leaking pipes, blocked drains, water pressure issues, toilet/bathroom fixtures, water heaters
- **Expected Resolution**: 1-3 hours (Emergency), 4-6 hours (High), 1-2 days (Medium/Low)
- **Required Skills**: Plumbing expertise, pipe fitting, drainage systems
- **Common Tools**: Plumbing tools, pipe fittings, drain cleaning equipment

##### **HVAC (Heating, Ventilation, Air Conditioning)**
- **Description**: Climate control and air quality issues
- **Examples**: AC not working, heating issues, ventilation problems, air quality concerns
- **Expected Resolution**: 2-6 hours (Emergency), 6-12 hours (High), 1-3 days (Medium/Low)
- **Required Skills**: HVAC technician, refrigeration knowledge, electrical basics
- **Common Tools**: HVAC tools, refrigerant, electrical testing equipment

##### **Structural & Civil**
- **Description**: Building structure, walls, doors, windows, and civil works
- **Examples**: Broken doors/windows, wall cracks, ceiling issues, flooring problems, structural damage
- **Expected Resolution**: 4-8 hours (Emergency), 1-2 days (High), 2-5 days (Medium/Low)
- **Required Skills**: Civil maintenance, carpentry, masonry
- **Common Tools**: Construction tools, building materials, measuring equipment

##### **Furniture & Fixtures**
- **Description**: Room furniture, fixtures, and interior fittings
- **Examples**: Broken beds/chairs, damaged wardrobes, loose fittings, furniture assembly
- **Expected Resolution**: 2-4 hours (High), 4-8 hours (Medium), 1-2 days (Low)
- **Required Skills**: Carpentry, furniture repair, assembly skills
- **Common Tools**: Carpentry tools, screws/bolts, wood glue, sandpaper

#### **4.2.2 IT & Technology Categories**

##### **Network & Internet**
- **Description**: Internet connectivity, WiFi, and network infrastructure issues
- **Examples**: No internet, slow connection, WiFi not working, network cable issues, router problems
- **Expected Resolution**: 1-2 hours (Emergency), 2-4 hours (High), 4-8 hours (Medium/Low)
- **Required Skills**: Network administration, router configuration, cable management
- **Common Tools**: Network cables, routers, network testing tools

##### **Computer & Hardware**
- **Description**: Desktop computers, laptops, and hardware peripherals
- **Examples**: Computer not starting, hardware failure, peripheral issues, software installation
- **Expected Resolution**: 2-4 hours (High), 4-8 hours (Medium), 1-2 days (Low)
- **Required Skills**: Computer hardware, troubleshooting, basic software knowledge
- **Common Tools**: Computer repair tools, diagnostic software, replacement parts

##### **Audio/Visual Equipment**
- **Description**: Projectors, speakers, display systems, and AV equipment
- **Examples**: Projector not working, audio issues, display problems, cable connections
- **Expected Resolution**: 1-3 hours (High), 3-6 hours (Medium), 6-12 hours (Low)
- **Required Skills**: AV equipment knowledge, cable management, basic electronics
- **Common Tools**: AV cables, projector lamps, audio equipment, testing devices

##### **Security Systems**
- **Description**: CCTV, access control, and security technology
- **Examples**: CCTV not working, access card issues, security system failures, camera problems
- **Expected Resolution**: 1-2 hours (Emergency), 2-4 hours (High), 4-8 hours (Medium/Low)
- **Required Skills**: Security system knowledge, camera installation, access control systems
- **Common Tools**: Security equipment, cables, mounting hardware, testing tools

#### **4.2.3 General Maintenance Categories**

##### **Housekeeping & Cleanliness**
- **Description**: Cleaning, sanitation, and general housekeeping issues
- **Examples**: Deep cleaning requests, pest control, garbage disposal, common area maintenance
- **Expected Resolution**: 2-4 hours (High), 4-8 hours (Medium), 1 day (Low)
- **Required Skills**: Cleaning techniques, sanitation knowledge, pest control
- **Common Tools**: Cleaning supplies, pest control materials, sanitation equipment

##### **Safety & Security**
- **Description**: Physical safety, security concerns, and emergency situations
- **Examples**: Broken locks, safety hazards, emergency lighting, fire safety equipment
- **Expected Resolution**: 30 minutes (Emergency), 1-2 hours (High), 2-4 hours (Medium/Low)
- **Required Skills**: Safety protocols, security systems, emergency procedures
- **Common Tools**: Safety equipment, locks, emergency supplies, testing devices

##### **Landscaping & Outdoor**
- **Description**: Garden maintenance, outdoor facilities, and external areas
- **Examples**: Garden maintenance, outdoor lighting, pathway issues, external building maintenance
- **Expected Resolution**: 4-8 hours (High), 1-2 days (Medium), 2-3 days (Low)
- **Required Skills**: Landscaping, outdoor maintenance, basic construction
- **Common Tools**: Gardening tools, outdoor equipment, maintenance supplies

#### **4.2.4 Custom Categories**
- **Description**: User-defined categories for unique or specialized issues
- **Assignment**: Manual assignment by admin only
- **Resolution Time**: Determined by admin based on issue complexity
- **Examples**: Special events setup, unique equipment issues, non-standard requests
- **Workflow**: 
  1. Student creates ticket with custom category
  2. Ticket remains unassigned until admin review
  3. Admin evaluates and manually assigns to appropriate staff
  4. Admin sets expected resolution time based on complexity

### **4.3 Advanced Features**

#### **4.3.1 User Management (Admin Only)**
- **User Creation**: Admin creates all user accounts (students, staff, other admins)
- **Role Assignment**: Assign appropriate roles and permissions
- **Profile Management**: Update user information and access rights
- **Account Activation/Deactivation**: Control user access to the system
- **Bulk User Operations**: Import/export user data, bulk updates

#### **4.3.2 Mapping Management (Admin Only)**
- **Staff-Hostel-Category Mapping**: Create associations between staff, hostels, and ticket categories
- **Mapping CRUD Operations**: Create, read, update, delete mapping configurations
- **Priority-based Mapping**: Set priority levels for staff assignments
- **Mapping Validation**: Ensure logical consistency in mappings
- **Mapping Analytics**: Track mapping effectiveness and utilization

#### **4.3.3 Analytics & Reporting**
- **Performance Dashboards**: Real-time metrics for all user roles
- **Trend Analysis**: Historical data analysis for pattern identification
- **Staff Performance**: Individual and team performance metrics
- **Issue Categories**: Analysis of most common issues by building/category
- **Resolution Time**: Average resolution time by category and priority
- **Student Satisfaction**: Feedback collection and analysis

#### **4.3.4 Resource Management**
- **Staff Scheduling**: Work schedule management and availability tracking
- **Inventory Tracking**: Basic inventory management for common repair items
- **Vendor Management**: External vendor coordination for specialized repairs
- **Cost Tracking**: Budget allocation and expense tracking per issue
- **Asset Management**: Equipment and infrastructure asset tracking

#### **4.3.5 Quality Assurance**
- **Feedback System**: Student rating and feedback collection
- **Quality Checks**: Admin verification before ticket closure
- **Reopening Logic**: Automatic reopening if issues recur within specified timeframe
- **Performance Reviews**: Regular staff performance evaluation
- **Continuous Improvement**: Process optimization based on feedback

---

## **5. BUSINESS LOGIC & WORKFLOWS**

### **5.1 Ticket Lifecycle Management**

#### **5.1.1 Ticket Creation Workflow**
```
Student Reports Issue
    ↓
System Validates Input
    ↓
Duplicate Check Performed
    ↓
Priority Auto-Assigned (with user override)
    ↓
Category Mapped to Staff Expertise
    ↓
Ticket Created with Unique ID
    ↓
Automated Assignment Triggered
    ↓
Notifications Sent to Relevant Parties
```

#### **5.1.2 Assignment Logic**
**Primary Assignment Criteria:**
1. **Expertise Match** (40% weight): Staff skills align with issue category
2. **Workload Balance** (30% weight): Current active tickets per staff member
3. **Location Proximity** (20% weight): Staff assigned to specific buildings
4. **Availability** (10% weight): Current shift and availability status

**Assignment Rules:**
- **Emergency Issues**: Immediately assigned to on-duty staff regardless of workload
- **Building-Specific**: Assigned to designated building wardens first
- **Specialized Skills**: Technical issues routed to appropriate specialists
- **Overflow Management**: Redistribution when staff reaches capacity limits

#### **5.1.3 Status Progression Rules**

| Current Status | Allowed Next Status | Trigger Condition | User Role |
|----------------|-------------------|------------------|-----------|
| Open | Assigned | Auto/Manual Assignment | System/Admin |
| Open | Cancelled | Issue invalid/duplicate | Admin |
| Assigned | In Progress | Staff accepts work | Staff/Admin |
| Assigned | On Hold | Waiting for resources/approval | Staff/Admin |
| Assigned | Cancelled | Issue invalid/duplicate | Admin |
| In Progress | On Hold | Waiting for resources/approval | Staff/Admin |
| In Progress | Resolved | Work completed | Staff/Admin |
| In Progress | Cancelled | Issue no longer valid | Admin |
| On Hold | In Progress | Resources available | Staff/Admin |
| On Hold | Resolved | Work completed | Staff/Admin |
| On Hold | Cancelled | Issue no longer valid | Admin |
| Resolved | Closed | Student confirms resolution | Student/Admin |
| Resolved | Reopened | Issue not properly resolved | Student/Admin |
| Closed | Reopened | Issue recurs or student unsatisfied | Student/Admin |
| Cancelled | Open | Issue becomes valid again | Admin |

### **5.2 Priority Management**

#### **5.2.1 Priority Levels**
- **Emergency**: Safety hazards, security issues, complete service outages
- **High**: Significant impact on daily life, partial service disruptions
- **Medium**: Moderate inconvenience, non-critical functionality issues
- **Low**: Minor issues, cosmetic problems, enhancement requests

#### **5.2.2 Priority Assignment Logic**
**Automatic Priority Assignment:**
- **Keywords Detection**: "emergency", "urgent", "safety", "security" → High/Emergency
- **Category Mapping**: Security issues → High, Housekeeping → Medium
- **Time Sensitivity**: Issues reported during exams → Priority boost
- **Location Impact**: Common areas → Higher priority than individual rooms

**Priority Escalation Rules:**
- **Time-based**: Medium → High after 24 hours, High → Emergency after 4 hours
- **Recurring Issues**: Automatic priority increase for repeated problems
- **Student Feedback**: Low satisfaction scores trigger priority increase

### **5.3 Escalation Management**

#### **5.3.1 Automatic Escalation Triggers**
- **Time Thresholds**: 
  - Emergency: 1 hour without assignment
  - High: 4 hours without progress
  - Medium: 24 hours without assignment
  - Low: 72 hours without assignment
- **SLA Breaches**: Approaching or exceeding service level agreements
- **Student Complaints**: Multiple follow-ups or dissatisfaction reports
- **Staff Unavailability**: Assigned staff unavailable for extended periods

#### **5.3.2 Escalation Hierarchy**
```
Level 1: Assigned Staff Member
    ↓ (Time/Performance threshold)
Level 2: Team Lead/Supervisor
    ↓ (Continued delays)
Level 3: Department Head/Warden
    ↓ (Critical issues)
Level 4: Hostel Administration
    ↓ (Emergency/Policy issues)
Level 5: Institute Administration
```

### **5.4 Assignment Algorithms**

#### **5.4.1 Multi-Staff Workload-Based Assignment Algorithm**
```
FOR each new ticket:
    1. Extract ticket details:
       - Hostel/Building location
       - Ticket category (or custom category flag)
       - Priority level
       - Expected resolution time based on category
    
    2. Handle custom categories:
       - If custom category: Mark for manual admin assignment
       - Skip automatic assignment and notify admin
       - End algorithm for custom categories
    
    3. Query eligible staff mappings:
       - Find ALL staff mapped to (Hostel + Category) combination
       - Include staff mapped to category across all hostels
       - Filter by availability, active status, and shift schedule
       - Include general maintenance staff as fallback
    
    4. Calculate workload scores for each eligible staff:
       - Current active tickets count
       - Estimated remaining work hours for active tickets
       - Staff capacity (based on role/experience level)
       - Recent performance metrics
       - Workload Score = (Active_Tickets × 0.4) + 
                         (Estimated_Hours × 0.3) + 
                         (Capacity_Utilization × 0.2) + 
                         (Performance_Factor × 0.1)
    
    5. Assignment decision:
       - Select staff with LOWEST workload score
       - If multiple staff have same score: Use round-robin
       - If all staff exceed capacity: Queue ticket and escalate
       - Emergency tickets: Override capacity limits for best available staff
    
    6. Fallback scenarios:
       - No exact mapping: Check category-only mappings
       - No category mapping: Assign to general maintenance staff
       - All staff busy: Create priority-based assignment queue
       - System failure: Escalate to admin for manual assignment
    
    7. Admin override capabilities:
       - Admin can assign/reassign to any staff regardless of mappings
       - Admin can modify workload calculations and capacity limits
       - Override assignments are logged with reason for audit
       - Admin can force-assign even when staff at capacity
```

#### **5.4.2 Load Balancing Rules**
- **Maximum Active Tickets**: 
  - Junior Staff: 5 active tickets
  - Senior Staff: 8 active tickets
  - Supervisors: 12 active tickets
- **Emergency Override**: Emergency tickets bypass load limits
- **Skill-based Distribution**: Complex issues to experienced staff
- **Geographic Distribution**: Building-specific assignments when possible

### **5.5 Quality Assurance Workflow**

#### **5.5.1 Resolution Verification**
```
Staff Marks Ticket as Resolved
    ↓
System Sends Notification to Student
    ↓
Student Has 24 Hours to Respond
    ↓
If Satisfied: Ticket Auto-Closed
    ↓
If Unsatisfied: Ticket Reopened with Comments
    ↓
Supervisor Reviews Reopened Tickets
    ↓
Additional Work Assigned or Issue Escalated
```

#### **5.5.2 Quality Metrics**
- **First-Time Resolution Rate**: Percentage of issues resolved without reopening
- **Student Satisfaction Score**: Average rating per staff member/category
- **Resolution Time Adherence**: Percentage of tickets resolved within SLA
- **Escalation Rate**: Percentage of tickets requiring escalation
- **Recurring Issue Rate**: Percentage of issues that recur within 30 days

---

## **6. USER EXPERIENCE DESIGN**

### **6.1 Student Experience**

#### **6.1.1 Issue Reporting Journey**
1. **Access Portal**: Single sign-on using institute credentials
2. **Quick Report**: Streamlined form with smart defaults
3. **Location Auto-fill**: Room number auto-populated from profile
4. **Category Assistance**: Guided category selection with examples
5. **Priority Guidance**: System suggests priority with explanation
6. **Immediate Confirmation**: Ticket number and expected resolution time
7. **Status Tracking**: Real-time updates via dashboard

#### **6.1.2 Dashboard Features**
- **My Tickets Only**: View only tickets created by the student
- **Quick Actions**: Report new issue, view recent updates
- **Status Timeline**: Visual progress tracking for each ticket
- **Communication**: View staff updates and add comments
- **Ticket Control**: Reopen or close their own tickets
- **Satisfaction Survey**: Rate completed work
- **Restricted Access**: Cannot view other students' tickets

### **6.2 Staff Experience**

#### **6.2.1 Work Management**
- **Assigned Tickets Only**: View only tickets assigned to them
- **Daily Dashboard**: Assigned tickets prioritized by urgency
- **Work Queue**: Organized list with filtering and sorting options
- **Ticket Details**: Complete issue information with history
- **Status Updates**: Update status of assigned tickets only
- **Time Tracking**: Automatic logging of work duration
- **Mobile Optimization**: Responsive design for tablet/mobile use
- **Restricted Access**: Cannot view unassigned or other staff's tickets

#### **6.2.2 Collaboration Tools**
- **Internal Comments**: Communication with supervisors and peers
- **Photo Documentation**: Before/after photos for work verification
- **Resource Requests**: Request additional materials or support
- **Reassignment Requests**: Request admin to reassign if unable to complete
- **Knowledge Base**: Access to common solutions and procedures

### **6.3 Supervisor Experience**

#### **6.3.1 Team Management**
- **Team Dashboard**: Overview of all team members and their workload
- **Assignment Control**: Manual assignment and reassignment capabilities
- **Performance Monitoring**: Real-time staff performance metrics
- **Quality Review**: Approval workflow for completed work
- **Escalation Management**: Handle escalated issues and complaints

#### **6.3.2 Operational Oversight**
- **Building Overview**: All issues within assigned buildings
- **Priority Management**: Adjust priorities based on operational needs
- **Resource Allocation**: Manage staff schedules and availability
- **Reporting Tools**: Generate performance and operational reports
- **Trend Analysis**: Identify recurring issues and improvement opportunities

### **6.3 Administrator Experience**

#### **6.3.1 Complete System Control**
- **All Tickets Access**: View, create, edit, delete any ticket
- **Assignment Control**: Assign/reassign tickets to any staff member
- **Status Management**: Update any ticket status regardless of assignment
- **Bulk Operations**: Mass updates, assignments, and status changes
- **Override Capabilities**: Bypass all system restrictions when needed

#### **6.3.2 User Management**
- **User Creation**: Create all types of user accounts (students, staff, admins)
- **Profile Management**: Update user information and access rights
- **Role Assignment**: Assign and modify user roles and permissions
- **Account Control**: Activate, deactivate, or delete user accounts
- **Bulk User Operations**: Import/export user data, mass updates

#### **6.3.3 Mapping Management**
- **Staff-Hostel-Category Mapping**: Create and manage all mappings
- **Mapping CRUD**: Full create, read, update, delete operations
- **Priority Settings**: Set assignment priorities for staff
- **Mapping Validation**: Ensure mapping consistency and effectiveness
- **Mapping Analytics**: Track and optimize mapping performance

#### **6.3.4 Strategic Dashboard**
- **Executive Summary**: High-level KPIs and performance metrics
- **Operational Analytics**: Detailed analysis of system performance
- **Resource Utilization**: Staff productivity and workload analysis
- **Cost Management**: Budget tracking and expense analysis
- **Trend Identification**: Long-term patterns and improvement opportunities

#### **6.3.5 System Management**
- **Configuration Control**: Adjust system parameters and workflows
- **Reporting Suite**: Comprehensive reporting and data export
- **Audit Trail**: Complete system activity logging
- **Performance Optimization**: System tuning and improvement recommendations
- **Data Management**: Backup, restore, and data integrity operations

---

## **7. BUSINESS RULES & CONSTRAINTS**

### **7.1 Operational Rules**

#### **7.1.1 Working Hours**
- **Regular Hours**: 8:00 AM - 6:00 PM (Monday-Saturday)
- **Emergency Coverage**: 24/7 for critical issues
- **Response Times**:
  - Emergency: 1 hour maximum
  - High Priority: 4 hours maximum
  - Medium Priority: 24 hours maximum
  - Low Priority: 72 hours maximum

#### **7.1.2 Assignment Constraints**
- **Building Assignment**: Staff primarily assigned to specific buildings
- **Skill Requirements**: Technical issues require certified technicians
- **Workload Limits**: Maximum active tickets per staff member
- **Availability Checks**: Real-time availability verification
- **Emergency Override**: Emergency issues bypass normal constraints

#### **7.1.3 Approval Requirements**
- **High-Cost Repairs**: Issues exceeding ₹5,000 require supervisor approval
- **External Vendors**: Third-party services require administrative approval
- **Infrastructure Changes**: Structural modifications need institute approval
- **Emergency Expenses**: Post-approval documentation for urgent repairs

### **7.2 Data Management Rules**

#### **7.2.1 Data Retention**
- **Active Tickets**: Retained indefinitely
- **Closed Tickets**: Retained for 3 years
- **User Activity**: Retained for 1 year
- **Performance Data**: Retained for 2 years
- **Audit Logs**: Retained for 5 years

#### **7.2.2 Privacy & Access Control**
- **Student Access**: Students can only view, reopen, and close their own tickets
- **Staff Access**: Staff can only view tickets assigned to them and update their status
- **Administrator Access**: Complete system access including:
  - View, create, edit, delete any ticket
  - Assign/reassign tickets to any staff
  - Update any ticket status
  - Create and manage all user accounts
  - Manage staff-hostel-category mappings
  - Access all system data and analytics
- **Data Export**: Restricted to administrators only
- **Audit Logging**: All admin actions logged for accountability

### **7.3 Performance Standards**

#### **7.3.1 Service Level Agreements**
- **System Availability**: 99.5% uptime during business hours
- **Response Time**: Page load times under 3 seconds
- **Data Accuracy**: 99.9% data integrity maintained
- **User Satisfaction**: Minimum 85% satisfaction score
- **Issue Resolution**: 90% first-time resolution rate

#### **7.3.2 Quality Metrics**
- **Resolution Time**: Average resolution within SLA targets
- **Student Satisfaction**: Minimum 4.0/5.0 rating
- **Staff Productivity**: Minimum 6 tickets resolved per day
- **Escalation Rate**: Maximum 10% of tickets escalated
- **System Adoption**: 95% user adoption within 3 months

---

## **8. INTEGRATION REQUIREMENTS**

### **8.1 Institute Systems**

#### **8.1.1 Student Information System**
- **User Authentication**: Single sign-on integration
- **Student Data**: Room assignments and contact information
- **Academic Calendar**: Exam periods for priority adjustments
- **Enrollment Status**: Active student verification

#### **8.1.2 HR Management System**
- **Staff Information**: Employee details and roles
- **Shift Schedules**: Work hours and availability
- **Performance Data**: Integration with HR performance reviews
- **Organizational Structure**: Reporting relationships and hierarchies

#### **8.1.3 Financial System**
- **Budget Tracking**: Maintenance expense allocation
- **Vendor Payments**: Integration with accounts payable
- **Cost Center Allocation**: Department-wise expense tracking
- **Approval Workflows**: Financial approval integration

### **8.2 Facility Management**

#### **8.2.1 Asset Management**
- **Equipment Registry**: Inventory of hostel assets
- **Maintenance Schedules**: Preventive maintenance planning
- **Warranty Information**: Equipment warranty tracking
- **Replacement Planning**: Asset lifecycle management

#### **8.2.2 Vendor Management**
- **Service Providers**: External contractor information
- **Service Agreements**: Contract terms and SLAs
- **Performance Tracking**: Vendor performance metrics
- **Payment Processing**: Automated invoice processing

---

## **9. SUCCESS METRICS & KPIs**

### **9.1 Operational Metrics**

#### **9.1.1 Efficiency Metrics**
- **Average Resolution Time**: Target reduction of 60%
- **First-Time Resolution Rate**: Target 90%
- **Staff Productivity**: Tickets resolved per staff per day
- **System Utilization**: Active user percentage
- **Process Automation**: Percentage of automated assignments

#### **9.1.2 Quality Metrics**
- **Student Satisfaction Score**: Target 4.5/5.0
- **Issue Recurrence Rate**: Target <5%
- **SLA Compliance**: Target 95%
- **Escalation Rate**: Target <10%
- **Data Accuracy**: Target 99.9%

### **9.2 Business Impact Metrics**

#### **9.2.1 Cost Optimization**
- **Operational Cost Reduction**: Target 25% reduction
- **Resource Utilization**: Staff efficiency improvement
- **Preventive Maintenance**: Reduction in emergency repairs
- **Vendor Cost Management**: Optimized external spending
- **Administrative Overhead**: Reduced manual processing

#### **9.2.2 Strategic Metrics**
- **Student Retention Impact**: Improved hostel satisfaction
- **Operational Excellence**: Benchmark against industry standards
- **Innovation Index**: Continuous improvement implementation
- **Scalability Factor**: System growth accommodation
- **ROI Achievement**: Return on investment within 18 months

---

## **10. RISK MANAGEMENT**

### **10.1 Operational Risks**

#### **10.1.1 System Risks**
- **System Downtime**: Backup systems and redundancy
- **Data Loss**: Regular backups and recovery procedures
- **Performance Degradation**: Monitoring and optimization
- **Security Breaches**: Access controls and audit trails
- **Integration Failures**: Fallback procedures and manual processes

#### **10.1.2 User Adoption Risks**
- **Resistance to Change**: Comprehensive training and support
- **Low Engagement**: Incentive programs and gamification
- **Skill Gaps**: Training programs and user support
- **Process Compliance**: Clear guidelines and enforcement
- **Communication Barriers**: Multi-language support if needed

### **10.2 Mitigation Strategies**

#### **10.2.1 Technical Mitigation**
- **Redundant Systems**: Backup servers and failover mechanisms
- **Regular Maintenance**: Scheduled system updates and optimization
- **Security Protocols**: Multi-factor authentication and encryption
- **Performance Monitoring**: Real-time system health monitoring
- **Disaster Recovery**: Comprehensive backup and recovery plans

#### **10.2.2 Organizational Mitigation**
- **Change Management**: Structured rollout and training programs
- **User Support**: Dedicated help desk and documentation
- **Process Documentation**: Clear procedures and guidelines
- **Regular Reviews**: Periodic system and process evaluation
- **Continuous Improvement**: Feedback-driven enhancements

---

## **11. IMPLEMENTATION ROADMAP**

### **11.1 Phase 1: Foundation (Months 1-2)**
- **Core System Setup**: Basic ticket creation and management
- **User Authentication**: Integration with institute systems
- **Basic Workflows**: Simple assignment and status tracking
- **Essential Reporting**: Basic dashboards and metrics
- **User Training**: Initial training for all user groups

### **11.2 Phase 2: Enhancement (Months 3-4)**
- **Advanced Assignment**: Smart assignment algorithms
- **Quality Management**: Feedback and rating systems
- **Performance Analytics**: Detailed reporting and analytics
- **Mobile Optimization**: Responsive design implementation
- **Integration Expansion**: Additional system integrations

### **11.3 Phase 3: Optimization (Months 5-6)**
- **Advanced Analytics**: Predictive analytics and trend analysis
- **Process Automation**: Workflow automation and optimization
- **Performance Tuning**: System optimization and scaling
- **Advanced Features**: Bulk operations and advanced workflows
- **Continuous Improvement**: Feedback-driven enhancements

### **11.4 Phase 4: Maturity (Months 7-12)**
- **Full Feature Set**: Complete system functionality
- **Performance Excellence**: Optimized performance and reliability
- **User Mastery**: Advanced user training and certification
- **Strategic Analytics**: Business intelligence and insights
- **Expansion Planning**: Scalability and future enhancements

---

## **12. BUSINESS LOGIC ENHANCEMENTS**

### **12.1 Advanced Assignment Logic**

#### **12.1.1 Multi-Dimensional Mapping System**
- **Hostel-Category Matrix**: Create a comprehensive mapping matrix where each staff member can be assigned to multiple hostel-category combinations
- **Priority Weighting**: Each mapping has a priority score (1-10) to handle overlapping assignments
- **Specialization Levels**: Staff can have different expertise levels (Beginner, Intermediate, Expert) for each category
- **Time-based Mappings**: Different staff assignments for different time periods (day/night shifts, weekends)

#### **12.1.2 Dynamic Assignment Rules**
- **Workload Intelligence**: Consider not just ticket count but also complexity and estimated resolution time
- **Historical Performance**: Factor in past performance metrics for similar issues
- **Geographic Optimization**: Minimize travel time by considering staff location and ticket location
- **Skill Escalation**: Automatically escalate to higher-skilled staff if initial assignment fails

### **12.2 Intelligent Workflow Management**

#### **12.2.1 Predictive Analytics**
- **Issue Prediction**: Identify potential issues before they occur based on historical patterns
- **Resource Forecasting**: Predict staff workload and resource requirements
- **Maintenance Scheduling**: Proactive maintenance scheduling based on issue patterns
- **Seasonal Adjustments**: Adjust assignment algorithms based on seasonal patterns (monsoon, exams, etc.)

#### **12.2.2 Smart Escalation System**
- **Multi-tier Escalation**: Automatic escalation through multiple levels based on issue complexity
- **Peer Assignment**: If primary staff unavailable, automatically assign to peer with similar skills
- **Emergency Protocols**: Special handling for emergency situations with immediate notifications
- **SLA Monitoring**: Real-time SLA tracking with proactive alerts before breaches

### **12.3 Quality Assurance Enhancements**

#### **12.3.1 Automated Quality Checks**
- **Photo Verification**: Require before/after photos for certain types of work
- **Time Validation**: Flag tickets with unusually short or long resolution times
- **Recurring Issue Detection**: Automatically identify and flag recurring issues
- **Student Satisfaction Triggers**: Automatic quality reviews for low-rated resolutions

#### **12.3.2 Performance Optimization**
- **Staff Performance Scoring**: Comprehensive scoring system based on multiple metrics
- **Training Recommendations**: Identify skill gaps and recommend training programs
- **Reward System**: Points-based system for high-performing staff
- **Peer Comparison**: Benchmarking against peer performance

### **12.4 Advanced User Experience Features**

#### **12.4.1 Smart Ticket Creation**
- **Template Suggestions**: Suggest ticket templates based on location and past issues
- **Auto-categorization**: AI-powered category suggestion based on description
- **Duplicate Prevention**: Advanced duplicate detection using similarity algorithms
- **Priority Intelligence**: Smart priority assignment based on multiple factors

#### **12.4.2 Communication Enhancement**
- **Status Notifications**: Automated notifications for all status changes
- **Progress Updates**: Regular progress updates for long-running tickets
- **Feedback Loops**: Structured feedback collection at multiple stages
- **Communication Templates**: Standardized communication templates for common scenarios

### **12.5 Resource Management Optimization**

#### **12.5.1 Inventory Integration**
- **Parts Tracking**: Track spare parts and materials usage
- **Automatic Reordering**: Trigger purchase orders when inventory runs low
- **Cost Allocation**: Accurate cost tracking per ticket and category
- **Vendor Management**: Integrated vendor performance tracking

#### **12.5.2 Capacity Planning**
- **Staff Utilization Analysis**: Detailed analysis of staff utilization patterns
- **Peak Time Management**: Special handling for peak periods (exam time, festivals)
- **Cross-training Recommendations**: Identify opportunities for staff cross-training
- **Resource Optimization**: Optimize resource allocation based on demand patterns

### **12.6 Analytics and Insights**

#### **12.6.1 Operational Intelligence**
- **Real-time Dashboards**: Live operational dashboards for all stakeholders
- **Trend Analysis**: Advanced trend analysis with predictive capabilities
- **Comparative Analytics**: Compare performance across different hostels/categories
- **Efficiency Metrics**: Comprehensive efficiency measurement and improvement tracking

#### **12.6.2 Strategic Insights**
- **Cost-Benefit Analysis**: ROI analysis for different improvement initiatives
- **Student Satisfaction Correlation**: Correlate operational metrics with student satisfaction
- **Preventive Maintenance ROI**: Measure the impact of preventive maintenance programs
- **Benchmarking**: Compare performance against industry standards

### **12.7 Compliance and Audit Features**

#### **12.7.1 Regulatory Compliance**
- **Safety Compliance**: Ensure all safety-related issues are handled according to regulations
- **Documentation Requirements**: Automatic documentation for compliance purposes
- **Audit Trail**: Comprehensive audit trail for all system activities
- **Reporting Standards**: Standardized reporting for regulatory requirements

#### **12.7.2 Quality Standards**
- **ISO Compliance**: Support for ISO quality management standards
- **Process Standardization**: Standardized processes for consistent service delivery
- **Continuous Improvement**: Built-in continuous improvement processes
- **Best Practice Sharing**: Knowledge sharing platform for best practices

### **12.8 Future-Ready Features**

#### **12.8.1 Scalability Enhancements**
- **Multi-campus Support**: Extend system to support multiple campuses
- **Integration Readiness**: APIs for integration with future systems
- **Modular Architecture**: Modular design for easy feature additions
- **Cloud Readiness**: Architecture suitable for cloud deployment

#### **12.8.2 Innovation Opportunities**
- **IoT Integration**: Prepare for IoT sensor integration for proactive monitoring
- **Mobile App Readiness**: Architecture ready for mobile app development
- **Voice Interface**: Potential for voice-based ticket creation
- **Chatbot Integration**: Framework for AI chatbot integration

---

## **13. KEY SYSTEM SPECIFICATIONS SUMMARY**

### **13.1 Access Control Matrix**

| User Role | Ticket Viewing | Ticket Creation | Status Updates | Assignment Control | User Management | Mapping Management |
|-----------|----------------|-----------------|----------------|-------------------|-----------------|-------------------|
| **Student** | Own tickets only | ✓ | Reopen/Close own tickets | ✗ | ✗ | ✗ |
| **Staff** | Assigned tickets only | ✗ | Assigned tickets only | ✗ | ✗ | ✗ |
| **Admin** | All tickets | ✓ | All tickets | Full control | Full control | Full control |

### **13.2 Assignment Logic Specifications**

#### **Primary Assignment Method**: Hostel-Category Mapping
1. **Exact Match**: Staff mapped to specific (Hostel + Category) combination
2. **Category Match**: Staff mapped to category across all hostels
3. **General Assignment**: Default maintenance staff
4. **Admin Override**: Manual assignment capability

#### **Mapping Structure**:
```
Multiple Staff Members ↔ Single (Hostel + Category) combination
Each staff can be mapped to multiple (Hostel + Category) combinations
Each mapping has priority level (1-10) and capacity weight
Assignment uses workload algorithm to select optimal staff from mapped pool
Admin can create/update/delete all mappings with real-time effect

Example Mapping:
Hostel: Block A, Category: Electrical
├── Staff 1: Electrician (Priority: 1, Capacity: 8 tickets)
├── Staff 2: Maintenance (Priority: 2, Capacity: 5 tickets)  
└── Staff 3: General (Priority: 3, Capacity: 3 tickets)

Custom Categories:
├── Always unassigned initially
├── Require manual admin assignment
└── No automatic mapping applies
```

### **13.3 Resolution Time Matrix**

#### **13.3.1 Category-Priority Based Resolution Times**

| Category | Emergency | High Priority | Medium Priority | Low Priority |
|----------|-----------|---------------|-----------------|--------------|
| **Electrical Issues** | 2-4 hours | 4-8 hours | 1-2 days | 2-3 days |
| **Plumbing & Water** | 1-3 hours | 4-6 hours | 1-2 days | 2-3 days |
| **HVAC** | 2-6 hours | 6-12 hours | 1-3 days | 3-5 days |
| **Structural & Civil** | 4-8 hours | 1-2 days | 2-5 days | 5-7 days |
| **Furniture & Fixtures** | N/A | 2-4 hours | 4-8 hours | 1-2 days |
| **Network & Internet** | 1-2 hours | 2-4 hours | 4-8 hours | 8-12 hours |
| **Computer & Hardware** | N/A | 2-4 hours | 4-8 hours | 1-2 days |
| **Audio/Visual Equipment** | N/A | 1-3 hours | 3-6 hours | 6-12 hours |
| **Security Systems** | 1-2 hours | 2-4 hours | 4-8 hours | 8-12 hours |
| **Housekeeping & Cleanliness** | N/A | 2-4 hours | 4-8 hours | 1 day |
| **Safety & Security** | 30 minutes | 1-2 hours | 2-4 hours | 4-6 hours |
| **Landscaping & Outdoor** | N/A | 4-8 hours | 1-2 days | 2-3 days |
| **Custom Categories** | Admin Defined | Admin Defined | Admin Defined | Admin Defined |

#### **13.3.2 Resolution Time Factors**
- **Complexity Level**: Simple, moderate, complex issues within same category
- **Resource Availability**: Parts, tools, external vendor requirements
- **Staff Expertise**: Skill level match with issue complexity
- **Time of Day**: Business hours vs. after-hours response
- **Seasonal Factors**: Weather-dependent issues, exam periods, festivals

#### **13.3.3 SLA Breach Handling**
- **Warning Alerts**: 75% of expected resolution time reached
- **Escalation Triggers**: 100% of expected resolution time exceeded
- **Emergency Override**: Critical issues bypass normal time expectations
- **Extension Requests**: Staff can request time extensions with justification
- **Admin Adjustments**: Real-time modification of expected resolution times

### **13.4 Workflow Specifications**

#### **Status Progression Rules**:
- **Students**: Can only reopen/close their own tickets
- **Staff**: Can update status of assigned tickets only
- **Admin**: Can update any ticket status, assign/reassign to anyone

#### **User Creation Process**:
- **All user accounts created by Admin only**
- **No self-registration capability**
- **Role-based access control enforced**

---

## **14. CONCLUSION**

The IIM Trichy Hostel Ticket Management System represents a comprehensive solution designed to transform hostel operations through intelligent automation, efficient workflows, and user-centric design. The system incorporates strict access controls, intelligent assignment algorithms, and comprehensive administrative capabilities.

**Key Differentiators:**
- **Strict Access Control**: Role-based access ensuring data privacy and security
- **Intelligent Assignment**: Hostel-category mapping system for optimal resource utilization
- **Administrative Control**: Complete system control for administrators
- **Scalable Architecture**: Designed for future enhancements and multi-campus expansion

The system's success will be measured through tangible improvements in resolution times, user satisfaction, and operational costs, while providing a foundation for continuous improvement and future enhancements. The phased implementation approach ensures minimal disruption while maximizing value delivery at each stage.

This product design document serves as the foundation for creating a world-class facility management system that will serve as a model for other educational institutions while directly contributing to the enhanced student experience at IIM Trichy.

---

**Document Version**: 1.0  
**Last Updated**: December 2024  
**Next Review**: March 2025  
**Document Owner**: Product Management Team  
**Stakeholders**: IIM Trichy Administration, Hostel Management, IT Department
