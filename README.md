# interaction-service
Interaction Service Assessment

### DB Design Choices
- **Didn't used relationships for authorId since it required to be either User or Bot.**
  - **Why** - It is given that **authorId** can either be User or Bot, so if we map a strict foreign key to either table it'll crash when other type tries to access it.

  
### Service Assumption
- **Assumption made** - The assignment doesn't specify a Virality Score for "Bot like". Therefore, I have assumed that there will be no Increment made on the action of a "Bot Like" only Interaction will be processed. **(Redis Increment will be skipped, treating it as +0 points)**

