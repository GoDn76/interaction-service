# interaction-service
Interaction Service Assessment

### DB Design Choices
- **Didn't used relationships for authorId since it required to be either User or Bot.**
  - **Why** - It is given that **authorId** can either be User or Bot, so if we map a strict foreign key to either table it'll crash when other type tries to access it.
