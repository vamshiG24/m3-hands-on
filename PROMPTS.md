# Module 3 – Hands-on Session Notes
**Course:** CS5013 – Programming with AI  
**Topic:** AI for Code Generation and Autocompletion  
**Instructor:** V. Krishna Nandivada, Department of CSE, IIT Madras  

---

## Part A – Ghost Text: Warm-up

### 1. Ghost text before opening `User.java`:
```java
public record UserDTO(Long id, String name, String email)
```
*Note: When `User.java` was closed, the inline AI model had only the name `UserDTO` to infer intent. It guessed generic, standard DTO fields (`Long id`, `String name`, `String email`) based on generic Java conventions.*

### 2. Ghost text after opening `User.java` in a second tab:
```java
public record UserDTO(long id, String name, String email, boolean active) {
```

### 3. Observation:
* **Does the ghost text change?**  
  **Yes.** As soon as `User.java` is opened in an adjacent editor tab, the AI tool (Copilot / inline completion engine) brings the file into its active context window (neighboring tabs context).
* It accurately identifies the four private fields defined in `User.java` (`long id`, `String name`, `String email`, and `boolean active`) and automatically suggests a record header mirroring those exact types and names.

---

## Part B – Mapper Boilerplate

### 1. Initial Ghost Text Completion for `fromUser`:
```java
public static UserDTO fromUser(User u) {
    return new UserDTO(u.getId(), u.getName(), u.getEmail(), u.getActive());
}
```

### 2. Compilation Error (Observed Hallucination):
```text
src/UserDTO.java:10: error: cannot find symbol
        return new UserDTO(u.getId(), u.getName(), u.getEmail(), u.getActive());
                                                                  ^
  symbol:   method getActive()
  location: variable u of type User
```
* **Hallucinated Method:** `u.getActive()`
* **Root Cause:** The assistant defaulted to standard JavaBean naming conventions (`getActive()`). However, `User.java` (line 25) names its boolean getter `isActive()`.

### 3. Manual Fix:
Replaced the hallucinated call with `u.isActive()`:
```java
public static UserDTO fromUser(User u) {
    return new UserDTO(u.getId(), u.getName(), u.getEmail(), u.isActive());
}
```

### 4. Verification (`main` output):
```java
public static void main(String[] args) {
    User user = new User(1L, "Alice", "alice@example.com", true);
    UserDTO dto = UserDTO.fromUser(user);
    System.out.println("Mapped UserDTO: " + dto);
}
```
**Execution output:**
```
Mapped UserDTO: UserDTO[id=1, name=Alice, email=alice@example.com, active=true]
```

---

## Part C – Controller Autofill (`OrderController.java`)

### 1. Ghost text triggered by typing `@GetMapping` / `@PostMapping`:
* When typing `@GetMapping("/{id}")`, the assistant suggested:
  ```java
  @GetMapping("/{id}")
  public ResponseEntity<Order> getOrderById(@PathVariable long id) {
      Order order = store.get(id);
      if (order == null) {
          return ResponseEntity.notFound().build();
      }
      return ResponseEntity.ok(order);
  }
  ```
* When typing `@PostMapping("/orders")`, the assistant suggested:
  ```java
  @PostMapping("/orders")
  public ResponseEntity<Order> createOrder(@RequestBody OrderRequest request) { ... }
  ```

### 2. Consistency Analysis with Hand-Written Endpoint (`listOrders()`):
* **Annotations:** **Inconsistent.** The hand-written endpoint `listOrders()` has no Spring framework annotations—it is a plain Java POJO controller documented via standard Javadoc (`/** GET /orders ... */`). The project lacks Spring Web dependencies.
* **Response Types:** **Inconsistent.** `listOrders()` returns `List<Order>` directly. The suggested `ResponseEntity<Order>` introduces HTTP wrapper types that break compilation and do not match the expected unit test contracts.
* **Status Codes:** **Inconsistent.** The hand-written code relies on plain object returns and returns `null` for missing records rather than HTTP 404.

### 3. Final Clean Implementation (Matching Tests):
```java
public Order getOrderById(long id) {
    return store.get(id);
}

public Order createOrder(String item, int qty) {
    Order order = new Order(nextId++, item, qty);
    store.put(order.id(), order);
    return order;
}
```

---

## Part D – AI-Assisted Git

### 1. Original AI-Generated Commit Message:
```text
Update UserDTO and OrderController

Add fromUser mapper, implement order endpoints, and update tests.
```
* **Critique:** The subject is generic and describes *what* changed rather than *why*. The body lists file modifications without explaining the architectural intent (DTO pattern for User data encapsulation and completing the in-memory order store).

### 2. Edited Commit Message (Honest & Informative):
```text
feat: implement UserDTO mapper and complete OrderController endpoints

- Convert UserDTO into a record mirroring User domain fields to safely expose user data without exposing internal mutable POJO state.
- Implement UserDTO.fromUser(User) mapping, adhering to User#isActive() boolean accessor convention.
- Implement getOrderById and createOrder in OrderController to fulfill in-memory order persistence for passing controller test suite.
```

### 3. Original AI-Generated PR Summary:
```markdown
## Summary of Changes
This pull request updates `UserDTO.java` and `OrderController.java`:
- Creates a `UserDTO` record and adds a `fromUser` mapper.
- Implements `getOrderById` and `createOrder` methods in `OrderController`.
- All tests pass.
```

### 4. Edited PR Summary (Honest & Context-Rich):
```markdown
## Purpose & Motivation
This PR completes the exercises for Session M3 on AI code autocompletion and mapper boilerplate:
1. **UserDTO Record & Mapper:** Encapsulates user details in an immutable Java record with a static factory `fromUser(User u)`. Resolves a classic AI hallucination by binding `u.isActive()` instead of `u.getActive()`.
2. **Order Controller Stubs:** Implements in-memory CRUD operations (`getOrderById` and `createOrder`) adhering to plain-Java controller semantics rather than over-engineered web annotations.
3. **Verification:** All 4/4 JUnit Jupiter tests in `OrderControllerTest` now pass cleanly.
```

---

## Part E – Branch-Name Suggestion

### 1. Issue:
> *"customer wants to be able to close their account permanently"*

### 2. Prompt Used:
```text
Suggest a branch name for this issue: "customer wants to be able to close their account permanently". Format: prefix/short-kebab-slug. Prefix is one of: feat, fix, chore, docs, refactor.
```

### 3. AI Suggestion:
```text
feat/permanent-account-closure
```

### 4. Comparison & Reflection:
* **Would I name it the same way?**  
  Not quite. While `feat/permanent-account-closure` accurately captures the requirement, in real-world production engineering, shorter and standard domain verbs are preferred, such as `feat/close-account` or `feat/account-deletion` (aligning with GDPR/retention terminology). However, the AI correctly chose the `feat/` prefix and followed kebab-case format.

---

## Deliverable Reflection: Where AI Understood vs. Misunderstood Intent

### 1. Where the AI Understood Intent:
* **Context Ingestion:** Once `User.java` was open in an editor tab, the AI accurately extracted field names, data types, and order, perfectly creating the record definition for `UserDTO`.
* **Standard Boilerplate:** In `OrderController`, the AI immediately recognized the idiomatic Java pattern for auto-incrementing in-memory map stores (`nextId++`, `store.put()`, `store.get()`).

### 2. Where the AI Did Not Understand Intent:
* **Accessor Convention Blindspot (Hallucinations):** The AI defaulted to standard JavaBeans getter syntax `getActive()` instead of checking the actual method declaration `isActive()`, causing a compilation error.
* **Architectural Over-engineering:** In Part C, upon seeing `@GetMapping`, the AI assumed a full Spring Boot web stack (`ResponseEntity`, `@PathVariable`) despite the file being a standalone, plain Java class with plain return types.
* **Commit Context:** The AI commit generator only summarized surface-level diffs ("what") but could not articulate the architectural reasoning ("why"). Human editing is essential to supply intent and domain rationale.
