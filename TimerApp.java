import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Timer;
import java.util.TimerTask;

public class TimerApp extends JFrame {

    private final JTextArea log = new JTextArea();
    private final Timer[] timers = new Timer[3];
    private final JButton[] startButtons = new JButton[3];

    public TimerApp() {
        super("Планировщик тренировки");

        setSize(700, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(3, 3, 10, 10));
        panel.setBorder(
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        );

        String[] names = {
                "Напомнить через 5 секунд",
                "Упражнение на 10 секунд",
                "Сигнал каждые 3 секунды"
        };

        for (int i = 0; i < 3; i++) {
            final int index = i;

            panel.add(new JLabel(names[i]));

            startButtons[i] = new JButton("Запустить");
            startButtons[i].addActionListener(e -> startTimer(index));
            panel.add(startButtons[i]);

            JButton stopButton = new JButton("Остановить");
            stopButton.addActionListener(e -> stopTimer(index));
            panel.add(stopButton);
        }

        log.setEditable(false);
        log.setFont(new Font("SansSerif", Font.PLAIN, 15));
        log.setMargin(new Insets(10, 10, 10, 10));

        add(panel, BorderLayout.NORTH);
        add(new JScrollPane(log), BorderLayout.CENTER);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                for (Timer timer : timers) {
                    if (timer != null) {
                        timer.cancel();
                    }
                }
            }
        });
    }

    private void write(String message) {
        log.append(message + "\n");
        log.setCaretPosition(log.getDocument().getLength());
    }

    private void startTimer(int index) {
        if (timers[index] != null) {
            return;
        }

        Timer timer = new Timer();
        timers[index] = timer;
        startButtons[index].setEnabled(false);

        if (index == 0) {
            write("Напоминание запущено. Ждём 5 секунд.");

            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    showResult(index, timer, () -> {
                        Toolkit.getDefaultToolkit().beep();
                        write("Напоминание: пора начинать тренировку!");
                        finishTimer(index);
                    });
                }
            }, 5000);

        } else if (index == 1) {
            write("Упражнение началось. Продолжительность — 10 секунд.");
            final long started = System.nanoTime();

            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    long elapsedSeconds =
                            (System.nanoTime() - started) / 1_000_000_000L;

                    long remaining = Math.max(0, 10 - elapsedSeconds);

                    showResult(index, timer, () -> {
                        if (remaining > 0) {
                            write("Упражнение: осталось " + remaining + " сек.");
                        } else {
                            Toolkit.getDefaultToolkit().beep();
                            write("Упражнение завершено!");
                            finishTimer(index);
                        }
                    });
                }
            }, 0, 1000);

        } else {
            write("Повторяющийся сигнал запущен. Период — 3 секунды.");

            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    showResult(index, timer, () -> {
                        Toolkit.getDefaultToolkit().beep();
                        write("Сигнал: меняем движение!");
                    });
                }
            }, 3000, 3000);
        }
    }

    private void showResult(int index, Timer source, Runnable action) {
        SwingUtilities.invokeLater(() -> {
            // Не показываем события от уже остановленного таймера.
            if (timers[index] == source) {
                action.run();
            }
        });
    }

    private void finishTimer(int index) {
        if (timers[index] != null) {
            timers[index].cancel();
            timers[index] = null;
        }

        startButtons[index].setEnabled(true);
    }

    private void stopTimer(int index) {
        if (timers[index] != null) {
            finishTimer(index);
            write("Таймер №" + (index + 1) + " остановлен вручную.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new TimerApp().setVisible(true);
        });
    }
}