package com.chess.game;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.chess.event.MyWindowEvent;
import com.chess.pieces.BlackPiece;
import com.chess.pieces.WhitePiece;
import com.chess.server.Server;
import com.chess.service.Judger;
import com.chess.timer.BlackTimer;
import com.chess.timer.WhiteTimer;
import com.chess.util.PointUtil;
import com.chess.util.ServerAndCilentUtil;

/**
 * ���������ķ���
 * 
 * @author ma
 *
 */
public class Room extends JFrame {
	public static final int WIDTH = 550;
	public static final int HEIGHT = 450;
	public static final int BOARD_WIDTH = 360;// ���̿��

	public static final int ROW_LINE_SPACE = 30; // ���߼��
	public static final int CRO_LINE_SPACE = 20; // ���߼��

	public static final int TIME = 5; // һ�ֵ�ʱ��
	
	///////////////////////// �������ͻ���ͨ����Ϣ
	public static final String STARTED = "true"; // ��Ϸ�Ѿ���
	public static final String CLOSE = "close"; // ����ر���Ϣ
	public static final String WIN = "win"; // һ����ʤ
	public static final String PEACE = "peace"; // һ�����
	public static final String REGRET = "regret"; // һ������
	public static final String SURRENDER = "surrender"; // һ������
	public static final String TIMEOVER = "timeover"; // һ��ʱ���þ�

	public static final String CONFIRM_TIMEOVER = "confirm timeover"; // ȷ��ʱ���þ�
	public static final String CONFIRM_REGRET = "confirm regret"; // ͬ�����
	public static final String CONFIRM_PEACE = "confirm peace"; // ͬ�����
	public static final String CONFIRM_WIN = "confirm win"; // ȷ���յ���ʤ��Ϣ
	public static final String CONFIRM_SURRENDER = "confirm surrender"; // ȷ���յ�������Ϣ
	
	public static final String REFUSE_REGRET = "refuse regret"; // �ܾ�����
	public static final String REFUSE_PEACE = "refuse peace"; // �ܾ�����
	
	////////////////////////
	private volatile boolean regret = false; // �Ƿ���Ի���

	public void setRegret(boolean regret) {
		this.regret = regret;
	}

	private static final int CHOOSE_COUNT = 3;
	private static final int TEXT_COUNT = 6;
	private static final int PANEL_COUNT = 2;

	private JPanel[] jps = new JPanel[PANEL_COUNT];
	private ChessBoard cb = new ChessBoard();

	public ChessBoard getCb() {
		return cb;
	}

	private JButton[] jbs = new JButton[CHOOSE_COUNT];
	private JLabel[] jls = new JLabel[TEXT_COUNT];

	public JLabel[] getJls() {
		return jls;
	}

	private ClickButton clickB = new ClickButton(); // �����ť
	private MyMouseClick mmc = new MyMouseClick(); // ���������

	private Server server; // �����
	private Socket socket; // �ͻ��˵�socket

	private MyWindowEvent defaultEvent;

	public Server getServer() {
		return server;
	}

	public Socket getSocket() {
		return socket;
	}

	public void setServer(Server server) {
		this.server = server;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}

	public Room() {
		try {
			init();
		} catch (UnknownHostException e) {
			JOptionPane.showMessageDialog(this, "��������ʧ��!");
		}
	}

	private void init() throws UnknownHostException {
		this.setTitle("������������,��������ip:" + InetAddress.getLocalHost().getHostAddress());
		this.setBounds(Game.X, Game.Y, WIDTH, HEIGHT);
		this.setLayout(null);

		cb.setBounds(0, 0, BOARD_WIDTH, HEIGHT);

		for (int i = 0; i < jps.length; i++) {
			jps[i] = new JPanel();
		}

		//////////////////// ��ʼ�����
		jps[0].setBounds(BOARD_WIDTH, 0, WIDTH - BOARD_WIDTH, HEIGHT / 3);
		jps[0].setBackground(Color.CYAN);
		jps[0].setLayout(new GridLayout(3, 2));

		jps[1].setBounds(BOARD_WIDTH, HEIGHT / 3, WIDTH - BOARD_WIDTH, HEIGHT - jps[0].getHeight());
		jps[1].setBackground(Color.blue);
		jps[1].setLayout(null);

		//////////////////// ��ʼ����ǩ
		jls[0] = new JLabel("����ʱ��: ");
		jls[1] = new JLabel(TIME + "����");
		jls[2] = new JLabel("�ڷ���ʱ: ");
		jls[3] = new JLabel("0��00��");
		jls[4] = new JLabel("�׷���ʱ: ");
		jls[5] = new JLabel("0��00��");

		for (int i = 0; i < jls.length; i++) {
			jps[0].add(jls[i]);
		}

		//////////////////// ��ʼ����ť
		jbs[0] = new JButton("����");
		jbs[1] = new JButton("���");
		jbs[2] = new JButton("����");

		int x = 405;
		int y = 180;
		for (int i = 0; i < jbs.length; i++) {
			jbs[i].setBounds(x, y, 100, 35);
			jbs[i].addActionListener(clickB);
			this.add(jbs[i]);
			y += 60;
		}

		cb.addMouseListener(mmc);
		this.add(cb);
		this.add(jps[0]);
		this.add(jps[1]);

		this.setResizable(false);
		this.setVisible(true);

	}

	private Graphics g;// ����

	public Graphics getG() {
		return g;
	}

	private MyWindowEvent serverEvent; // ����˴����¼�
	private MyWindowEvent cilentEvent; // �ͻ��˴����¼�

	private List<Point> blackList;
	private List<Point> whiteList;

	/**
	 * �ȴ���Ҽ�����Ϸ
	 * 
	 * @param room
	 */
	public void waitPlayer() {
		regret = false;
		defaultEvent = new MyWindowEvent(server.getServer(), server.getServerSocket(), this);
		this.addWindowListener(defaultEvent);

		for (int i = 0; i < jbs.length; i++) {
			jbs[i].setEnabled(false);

		}

		Game.getThreadPool().execute(new Runnable() {

			@Override
			public void run() {
				while (!Game.connect) {

				}
				startGame(BlackPiece.BLACK);
			}
		});
	}

	private BlackTimer bTimer;
	private WhiteTimer wTimer;
	
	public void initTime() {
		jls[3].setText("00:00");
		jls[5].setText("00:00");
	}
	
	/**
	 * ��ʼ��Ϸ
	 */
	public void startGame(int color) {
		initTime();
		
		bTimer = new BlackTimer(this);
		bTimer.start();
		
		wTimer = new WhiteTimer(this);
		wTimer.start();
		
		this.removeWindowListener(defaultEvent);

		for (int i = 0; i < jbs.length; i++) {
			jbs[i].setEnabled(true);

		}

		if (color == BlackPiece.BLACK) {
			Judger.mover = BlackPiece.BLACK;// �ڷ�
			serverEvent = new MyWindowEvent(server.getServer(), server.getServerSocket(), this);
			this.addWindowListener(serverEvent);
			JOptionPane.showMessageDialog(this, "����ҽ�������Ϸ");

		} else if (color == WhitePiece.WHITE) {
			Judger.mover = WhitePiece.WHITE;// �׷�
			// ����
			WhitePiece.isDown = true;
			BlackPiece.isDown = true;

			cilentEvent = new MyWindowEvent(socket, this);
			this.addWindowListener(cilentEvent);
			JOptionPane.showMessageDialog(this, "�ɹ�������Ϸ");
		}

		g = cb.getGraphics();
		blackList = ChessBoard.getBlackList();
		whiteList = ChessBoard.getWhiteList();
	}

	// ��ʾ����λ��
	private static int[][] pieces = ChessBoard.getPieces();

	class MyMouseClick extends MouseAdapter {

		@Override
		public void mouseClicked(MouseEvent e) {
			if (!Game.connect)
				return;

			int x = e.getX();
			int y = e.getY();

			int[] point = PointUtil.judgePoint(x, y);
			if (point != null && Judger.winner == Judger.UNKNOW && Judger.escaper == Judger.UNKNOW) {
				boolean flag = false;

				if (BlackPiece.isDown == false && Judger.mover == BlackPiece.BLACK) { // �ڷ��ߵ�
					try {
						pieces[point[0] / 20][point[1] / 30] = BlackPiece.BLACK; // ��λ����
						blackList.add(new Point(point[0], point[1]));

						if (blackList.size() > 4) {
							int winner = Judger.judgeWinner();
							if (winner == BlackPiece.BLACK) { // ������ʤ
								Judger.winner = BlackPiece.BLACK;
							}

						}

						ServerAndCilentUtil.sendPoint(point[0], point[1], server.getSocket());

					} catch (IOException e1) {

					} finally {
						regret = true;
						BlackPiece.isDown = true; // �ڷ��Ѿ�����
						flag = true;
					}

				} else if (WhitePiece.isDown == false && BlackPiece.isDown && Judger.mover == WhitePiece.WHITE) {// �ڷ����꣬�׷���
					try {
						pieces[point[0] / 20][point[1] / 30] = WhitePiece.WHITE; // ��λ����
						whiteList.add(new Point(point[0], point[1]));

						if (whiteList.size() > 4) {
							int winner = Judger.judgeWinner();

							if (winner == WhitePiece.WHITE) { // �׷���ʤ
								Judger.winner = WhitePiece.WHITE;
							}

						}

						ServerAndCilentUtil.sendPoint(point[0], point[1], socket);

					} catch (IOException e1) {
					} finally {
						regret = true;
						WhitePiece.isDown = true; // �׷�����
						BlackPiece.isDown = false; // �ڷ�δ��
						flag = true;
					}
				}

				if (flag) { // ��Ч�ĵ��
					PointUtil.removePoint(point[0], point[1]);
					g.fillOval(point[0] - 10, point[1] - 10, 20, 20);// �԰뾶��Բ
				}

			}
		}

	}

	class ClickButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (!Game.connect)
				return;

			if (e.getSource() == jbs[0]) {
				int confirm = JOptionPane.showConfirmDialog(Room.this, "��ȷ��������?");

				if (confirm == 0) { // ȷ������
					try {
						if (Judger.mover == BlackPiece.BLACK) { // �ڷ�����
							Socket s = server.getSocket();

							ServerAndCilentUtil.sendConfirmInfo(SURRENDER, s);

							Judger.winner = WhitePiece.WHITE;

						} else { // �׷�����
							ServerAndCilentUtil.sendConfirmInfo(SURRENDER, socket);

							Judger.winner = BlackPiece.BLACK;
						}
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}

			} else if (e.getSource() == jbs[1]) {
				int confirm = JOptionPane.showConfirmDialog(Room.this, "��ȷ��Ҫ�����?");

				if (confirm == 0) { // ȷ�����
					try {
						if (Judger.mover == BlackPiece.BLACK) { // �ڷ����
							Socket s = server.getSocket();

							ServerAndCilentUtil.sendConfirmInfo(PEACE, s);

						} else { // �׷����
							ServerAndCilentUtil.sendConfirmInfo(PEACE, socket);

						}
					} catch (IOException e1) {

					}
				}
				
			} else if (e.getSource() == jbs[2]) {
				if (regret) { // ���Ի���
					int confirm = JOptionPane.showConfirmDialog(Room.this, "��ȷ��Ҫ������?");
					
					if (confirm == 0) { // ȷ�ϻ���
						try {
							if (Judger.mover == BlackPiece.BLACK) { // �ڷ��������
								Socket s = server.getSocket();
								
								ServerAndCilentUtil.sendConfirmInfo(REGRET, s);
								
							} else { // �׷��������
								ServerAndCilentUtil.sendConfirmInfo(REGRET, socket);
								
							}
						} catch (IOException e1) {
							
						}
						
					}

				} else {
					JOptionPane.showMessageDialog(Room.this, "���ڲ��ܻ���");
				}
			}
		}
	}
}
