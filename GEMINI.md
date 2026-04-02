# HamBooking - Project Mandates

## 1. Documentation Standards
- **Javadoc:** All classes, fields, and public methods must have descriptive Javadoc comments.
- **No Inline Comments:** Avoid using inline comments (`//`) for primary documentation or metadata descriptions. Use Javadoc blocks instead.
- **Language:** Documentation must be in Spanish (as per project requirements for TFG) or English if specified, but consistent. In this project, we prioritize Spanish for the defense.

## 2. Engineering Standards
- **JPA & Hibernate:** Use `FetchType.LAZY` for all collections and associations unless `EAGER` is strictly required.
- **Validation:** Maintain "Defense in Depth" by validating both in Java (Bean Validation) and SQL (Constraints).
- **Lombok:** Use Lombok to reduce boilerplate, but ensure Javadoc is present for the class and key fields.
