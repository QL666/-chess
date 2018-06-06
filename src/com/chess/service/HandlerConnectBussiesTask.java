package com.chess.service;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.HeadlessException;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import javax.swing.JOptionPane;

import com.chess.game.ChessBoard;
import com.chess.game.Game;
import com.chess.game.Room;
import com.chess.pieces.BlackPiece;
import com.chess.pieces.WhitePiece;
import com.chess.server.Server;
import com.chess.util.CloseResource;
import com.chess.util.PointUtil;
import com.chess.util.ServerAndCilentUtil;

public class HandlerConnectBussiesTask implements Runnable {
	private ServerSocket server;
	private ServerSocket serverSocket;
	private Socket socket;
	private Room room;

	public HandlerConnectBussiesTask(ServerSocket server, ServerSocket serverSocket, Room room) {
		this.server = server;
		this.serverSocket = serverSocket;
		this.room = room;
	}

	public Socket getSocket() {
		return socket;
	}

	@Override
	public void run() {
		try {
			boolean surrender = false;
			List<Point> whiteList = ChessBoard.getWhiteList();
			
			while (true) {
				socket = server.accept();
				
				Game.connect = true;
				
				while (Judger.winner == Judger.UNKNOW) { // û�л�ʤ��
					InputStream inputStream = socket.getInputStream();
					
					BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
					
					String info = reader.readLine();
					
					if (info != null) {
						if (Room.CLOSE.equals(info)) { // ���ӷ�����
							Judger.escaper = WhitePiece.WHITE;
							break;
						}
						
						if (Room.CONFIRM_WIN.equals(info)) { // ���յ��ͻ��˷��͵�ȷ�Ϸ����ʤ������Ϣ
							
							break;
						}
						
						if (Room.SURRENDER.equals(info)) { // �յ����ӷ�������Ϣ
							ServerAndCilentUtil.sendConfirmInfo(Room.CONFIRM_SURRENDER, socket); // ����һ��ȷ���յ����ӷ���������Ϣ
							
							surrender = true;
							Judger.winner = BlackPiece.BLACK;
							break;
						}
						
						if (Room.CONFIRM_SURRENDER.equals(info)) { // �������Ϣ�õ�ȷ��
							
							break;
						}
						
						if (Room.PEACE.equals(info)) { // �Է��������
							int confirm = JOptionPane.showConfirmDialog(room, "�Է��������,�Ƿ�ͬ��?");
							
							if (confirm == 0) { // ͬ�����
								ServerAndCilentUtil.sendConfirmInfo(Room.CONFIRM_PEACE, socket); // ����ͬ�������Ϣ���ͻ���
								
								Judger.peace = true;
								break;
								
							} else { // �ܾ�����
								ServerAndCilentUtil.sendConfirmInfo(Room.REFUSE_PEACE, socket);
								
								continue;
							}
							
						}
						
						if (Room.CONFIRM_PEACE.equals(info)) { // �Է�ͬ�����
							Judger.peace = true;
							break;
							
						}
						
						if (Room.REFUSE_PEACE.equals(info)) { // �Է���ͬ�����
							JOptionPane.showMessageDialog(room, "�Է���ͬ����ĺ�������!");
							continue;
						}
						
						if (Room.REGRET.equals(info)) {   // �Է��������
							int confirm = JOptionPane.showConfirmDialog(room, "�Է��������,�Ƿ�ͬ��?");
							
							if (confirm == 0) { 
								ServerAndCilentUtil.sendConfirmInfo(Room.CONFIRM_REGRET, socket);  // ����ͬ�������Ϣ
								
								Point point = whiteList.remove(whiteList.size() - 1); // ɾ�����һ������
								/// ���½��������뼯��
								List<Point> points = ChessBoard.getPoints();
								points.add(point);
								
								// �ػ�
								room.getCb().repaint();
								
								// ����
								BlackPiece.isDown = true;
								
							} else { // �ܾ��Է�����
								ServerAndCilentUtil.sendConfirmInfo(Room.REFUSE_REGRET, socket);
							}
							
							continue;
							
						}
						
						
						if (Room.CONFIRM_REGRET.equals(info)) { // �Է�ͬ�����
							// ɾ�����һ������
							List<Point> blackList = ChessBoard.getBlackList();
							Point point = blackList.remove(blackList.size() - 1);
							
							/// ���½��������뼯��
							List<Point> points = ChessBoard.getPoints();
							points.add(point);
							
							// �ػ�
							room.getCb().repaint();
							
							// ����
							room.setRegret(false);
							BlackPiece.isDown = false;
							continue;
						}
						
						if (Room.REFUSE_REGRET.equals(info)) { // �Է���ͬ�����
							JOptionPane.showMessageDialog(room, "�Է���ͬ����Ļ�������!");
							
							continue;
						}
						
						if (Room.TIMEOVER.equals(info)) { // ʱ��ľ�
							
							// ����ȷ����Ϣ
							ServerAndCilentUtil.sendConfirmInfo(Room.CONFIRM_TIMEOVER, socket);
							
							break;
							
						}
						
						if (Room.CONFIRM_TIMEOVER.equals(info)) {
							break;
						}
						
						String[] split = info.split(",");
						
						Graphics g = room.getG();
						
						g.setColor(Color.WHITE);
						g.fillOval(Integer.parseInt(split[0]) - 10, Integer.parseInt(split[1]) - 10, 20, 20);
						g.setColor(Color.BLACK);
						room.setVisible(true);
						whiteList.add(new Point(Integer.parseInt(split[0]), Integer.parseInt(split[1])));
						
						if (split.length > 2) { // �׷���ʤ
							// ����һ��ȷ�ϰ׷�ʤ������Ϣ
							ServerAndCilentUtil.sendConfirmInfo(Room.CONFIRM_WIN, socket);
							
							Judger.winner = WhitePiece.WHITE;
							break;
						}
						
						PointUtil.removePoint(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
						
						room.setRegret(false);
						BlackPiece.isDown = false;
					}
					
					
				}
				int confirm = -1;    //// 0 1 2 -1
				if (Judger.escaper == WhitePiece.WHITE) {
					confirm = JOptionPane.showConfirmDialog(room, "�׷��������,������ʤ��!�Ƿ����?");
					
				}
				
				if (Judger.peace == true) {
					confirm = JOptionPane.showConfirmDialog(room, "����!�Ƿ����?");
				}
				
				if (Judger.timeOver == BlackPiece.BLACK) {
					confirm = JOptionPane.showConfirmDialog(room, "ʱ���þ�!������,�Ƿ����?");
				}
				
				if (Judger.timeOver == WhitePiece.WHITE) {
					confirm = JOptionPane.showConfirmDialog(room, "�Է�ʱ���þ�!������ʤ��,�Ƿ����?");
				}
				
				if (Judger.winner == BlackPiece.BLACK) {
					if (surrender) {
						confirm = JOptionPane.showConfirmDialog(room, "�׷�Ͷ��,������ʤ��!�Ƿ����?");
					} else {
						confirm = JOptionPane.showConfirmDialog(room, "������ʤ��!�Ƿ����?");
						
					}
					
				}
				
				if (Judger.winner == WhitePiece.WHITE && Judger.escaper != BlackPiece.BLACK) {
					confirm = JOptionPane.showConfirmDialog(room, "������!�Ƿ����?");
					
				}
				if (confirm != 0) break;
				
				//////��ʼ��
				Game.initProperty();
				room.getCb().repaint();
				room.waitPlayer();
				room.initTime();
			}
			

		} catch (IOException e) {
		} finally {
			CloseResource.close(socket);
			CloseResource.close(server);
			CloseResource.close(serverSocket);
			
			room.setVisible(false);
			CloseResource.free(room);
			
			new Game();
		}
	}

}
