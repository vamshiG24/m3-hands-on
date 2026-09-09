/**
 * Session M3 Part A / B: replace this empty record header with the
 * fields Copilot suggests once you re-trigger ghost text after
 * opening User.java in a second tab. Then add a static
 * {@code fromUser(User)} mapper as described in Part B.
 */
public record UserDTO(long id, String name, String email, boolean active) {

    public static UserDTO fromUser(User u) {
        return new UserDTO(u.getId(), u.getName(), u.getEmail(), u.isActive());
    }

    public static void main(String[] args) {
        User user = new User(1L, "Alice", "alice@example.com", true);
        UserDTO dto = UserDTO.fromUser(user);
        System.out.println("Mapped UserDTO: " + dto);
    }
}
