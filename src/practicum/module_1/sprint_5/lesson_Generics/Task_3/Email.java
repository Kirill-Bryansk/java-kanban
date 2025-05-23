package practicum.module_1.sprint_5.lesson_Generics.Task_3;

// унаследуйте класс от базового класса, описывающего контакт Contact
public class Email extends Contact {
    private final String email;
    public Email(String name, String email) {
        super(name);
        this.email = email;
    }
    public String getEmail() {
        return email;
    }
    @Override
    public void sendMessage() {
        System.out.println("Отправим новогоднюю картинку коллеге на электронную почту " + email);
    }
    @Override
    public void print() {
        System.out.println("Email: " + getEmail());
    }
}
