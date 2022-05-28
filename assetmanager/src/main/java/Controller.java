public class Controller {

    public static void main(String[] args) throws InterruptedException {
        if ("EXECUTE".equals(args[0])) {
            Thread t = new Thread(new Executor());
            while (true) {
                t.run();
                Thread.sleep(60000);
            }
        } else if ("SIMULATE".equals(args[0])) {
            Thread t = new Thread(new Simulator());
            t.run();
        }
    }
}
