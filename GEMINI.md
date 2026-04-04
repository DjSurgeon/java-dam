# HamBooking - Project Mandates

## 1. Documentation Standards
- **Javadoc:** All classes, fields, and public methods must have descriptive Javadoc comments.
- **No Inline Comments:** Avoid using inline comments (`//`) for primary documentation or metadata descriptions. Use Javadoc blocks instead.
- **Language:** Documentation must be in Spanish (as per project requirements for TFG) or English if specified, but consistent. In this project, we prioritize Spanish for the defense.

## 2. Commit Standards
- **Format:** Use the Conventional Commits style: `<type>(<scope>): <description>`.
- **Types:**
    - `feat`: New features.
    - `fix`: Bug fixes.
    - `refactor`: Code changes that neither fix a bug nor add a feature.
    - `docs`: Documentation only changes.
    - `style`: Changes that do not affect the meaning of the code (white-space, formatting, etc).
    - `test`: Adding missing tests or correcting existing tests.
- **Language:** Commit messages must be in English.
- **Scope:** Use the relevant module or layer (e.g., `model`, `service`, `ui`, `api`).

## 3. Engineering Standards
- **JPA & Hibernate:** Use `FetchType.LAZY` for all collections and associations unless `EAGER` is strictly required.
- **Validation:** Maintain "Defense in Depth" by validating both in Java (Bean Validation) and SQL (Constraints).
- **Lombok:** Use Lombok to reduce boilerplate, but ensure Javadoc is present for the class and key fields.
