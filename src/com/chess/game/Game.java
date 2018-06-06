package com.chess.game;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.chess.pieces.BlackPiece;
import com.chess.pieces.WhitePiece;
import com.chess.server.Server;
import com.chess.service.Judger;
import com.chess.service.ReceiveServerTask;
import com.chess.util.CloseResource;

public class Game extends JFrame {
	public static final int X = 350;
	public static final int Y = 200;
	private static final int WIDTH = 350;
	private static final int HEIGHT = 350;

	private static final int CHOOSE_COUNT = 3;

	private JPanel jp = new JPanel();
	private JButton[] jbs = new JButton[CHOOSE_COUNT];

	public static final int PORT = 9999;
	public static final int PORT2 = 10000;
	public static volatile boolean connect = false; // �Ƿ�����������

	public static final int WIN_COUNT = 5; 
	
	private static ExecutorService threadPool = Executors.newCachedThreadPool(); // �̳߳�

	private Server server;
	private Socket socket; // �ͻ��˵�socket
	
	public static ExecutorService getThreadPool() {
		return threadPool;
	}

	public Game() {
		this.setTitle("������������");
		this.setBounds(X, Y, WIDTH, HEIGHT);
		jp.setLayout(null);

		jbs[0] = new JButton("������Ϸ");
		jbs[1] = new JButton("������Ϸ");
		jbs[2] = new JButton("�˳���Ϸ");

		int x = 125;
		int y = 30;
		for (int i = 0; i < jbs.length; i++) {
			jbs[i].setBounds(x, y, 100, 35);
			jbs[i].addActionListener(new Click());
			jp.add(jbs[i]);

			y += 80;
		}

		this.add(jp);
		this.setResizable(false);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setVisible(true);
	}

	/**
	 * ��ʼ��ϵ������
	 */
	public static void initProperty() {
		connect = false;
		Judger.winner = Judger.UNKNOW;
		Judger.escaper = Judger.UNKNOW;
		Judger.peace = false;
		Judger.timeOver = Judger.UNKNOW;
		BlackPiece.isDown = false;
		WhitePiece.isDown = false;
		
		ChessBoard.init();
	}

	/**
	 * ����Ƿ��Ѿ�����������
	 */
	private void checkConnected(String ipAddr) throws Exception {
		Socket socket = new Socket(ipAddr, Game.PORT2);

		InputStream inputStream = socket.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

		String info = reader.readLine();
		socket.close();

		if (Room.STARTED.equals(info)) {
			throw new Exception();
		}
	}

	private class Click implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == jbs[0]) {
				String ipAddr = JOptionPane.showInputDialog("���������ӵ�ip��ַ");

				if (ipAddr == null || "".equals(ipAddr)) {
					return;
				}
				initProperty();
				try {
					checkConnected(ipAddr);

					socket = new Socket(ipAddr, Game.PORT);

					Game.this.setVisible(false);

					Room room = new Room();
					room.setSocket(socket);
					room.startGame(WhitePiece.WHITE);
					connect = true;

					threadPool.execute(new ReceiveServerTask(socket, room));

					CloseResource.free(Game.this);

				} catch (Exception e1) {
					JOptionPane.showMessageDialog(Game.this, "��������ʧ��");
					Game.this.setVisible(true);

				} finally {

				}

			} else if (e.getSource() == jbs[1]) {
				initProperty();
				Room room = new Room();
				try {
					server = new Server(room);
					room.setServer(server);
					room.waitPlayer();

				} catch (IOException e1) {
					room.setVisible(false);
					CloseResource.free(room);
					JOptionPane.showMessageDialog(Game.this, "��������ʧ��");

					return;
				}

				Game.this.setVisible(false);
				CloseResource.free(Game.this);


			} else if (e.getSource() == jbs[2]) {
				System.exit(NORMAL);

			}
		}


	}
}
