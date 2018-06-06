package com.chess.service;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.List;

import javax.swing.JOptionPane;

import com.chess.game.ChessBoard;
import com.chess.game.Game;
import com.chess.game.Room;
import com.chess.pieces.BlackPiece;
import com.chess.pieces.WhitePiece;
import com.chess.util.CloseResource;
import com.chess.util.PointUtil;
import com.chess.util.ServerAndCilentUtil;

public class ReceiveServerTask implements Runnable {
	private Socket socket;
	private Room room;

	public ReceiveServerTask(Socket socket, Room room) {
		this.socket = socket;
		this.room = room;
	}

	@Override
	public void run() {
		boolean surrender = false;
		try {
			List<Point> blackList = ChessBoard.getBlackList();
			while (Judger.winner == Judger.UNKNOW) { // û�л�ʤ��һֱ����
				InputStream inputStream = socket.getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

				String info = reader.readLine();
				if (info != null) {
					if (Room.CLOSE.equals(info)) { // ��������
						Judger.escaper = BlackPiece.BLACK;
						break;
					}

					if (Room.CONFIRM_WIN.equals(info)) { // ���յ�����˷��͵�ȷ�Ͽͻ���ʤ������Ϣ
						break;
					}

					if (Room.SURRENDER.equals(info)) { // �յ�������������Ϣ
						// ����һ��ȷ���յ��������������Ϣ
						ServerAndCilentUtil.sendConfirmInfo(Room.CONFIRM_SURRENDER, socket);

						surrender = true;
						Judger.winner = WhitePiece.WHITE;
						break;
					}

					if (Room.CONFIRM_SURRENDER.equals(info)) { // ���͸�������������Ϣ�õ�ȷ��
						break;

					}

					if (Room.PEACE.equals(info)) { // �յ������������Ϣ
						int confirm = JOptionPane.showConfirmDialog(room, "�Է��������,�Ƿ�ͬ��?");

						if (confirm == 0) { // ͬ�����
							ServerAndCilentUtil.sendConfirmInfo(Room.CONFIRM_PEACE, socket); // ����ͬ�������Ϣ�������

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

					if (Room.REGRET.equals(info)) { // �Է��������
						int confirm = JOptionPane.showConfirmDialog(room, "�Է��������,�Ƿ�ͬ��?");

						if (confirm == 0) {
							ServerAndCilentUtil.sendConfirmInfo(Room.CONFIRM_REGRET, socket); // ����ͬ�������Ϣ

							Point point = blackList.remove(blackList.size() - 1); // ɾ�����һ������
							/// ���½��������뼯��
							List<Point> points = ChessBoard.getPoints();
							points.add(point);

							// �ػ�
							room.getCb().repaint();

							// ����
							WhitePiece.isDown = true;
							BlackPiece.isDown = false;
							
						} else { // ��ͬ�����
							ServerAndCilentUtil.sendConfirmInfo(Room.REFUSE_REGRET, socket);
							
						}

						continue;
					}

					if (Room.CONFIRM_REGRET.equals(info)) { // �Է�ͬ�����
						// ɾ�����һ������
						List<Point> whiteList = ChessBoard.getWhiteList();
						Point point = whiteList.remove(whiteList.size() - 1);

						/// ���½��������뼯��
						List<Point> points = ChessBoard.getPoints();
						points.add(point);

						// �ػ�
						room.getCb().repaint();

						// ����
						room.setRegret(false);
						WhitePiece.isDown = false;
						BlackPiece.isDown = true;
						continue;

					}
					
					if (Room.REFUSE_REGRET.equals(info)) { // �Է���ͬ�����
						JOptionPane.showMessageDialog(room, "�Է���ͬ����Ļ�������!");
						
						continue;
					}
					
					if (Room.TIMEOVER.equals(info)) { // ʱ���þ�
						ServerAndCilentUtil.sendConfirmInfo(Room.CONFIRM_TIMEOVER, socket);
						
						break;
						
					}
					
					if (Room.CONFIRM_TIMEOVER.equals(info)) {
						break;
					}

					String[] split = info.split(",");
					Graphics g = room.getG();
					g.setColor(Color.BLACK);
					g.fillOval(Integer.parseInt(split[0]) - 10, Integer.parseInt(split[1]) - 10, 20, 20);
					room.setVisible(true);
					blackList.add(new Point(Integer.parseInt(split[0]), Integer.parseInt(split[1])));

					if (split.length > 2) { // ��������ʤ
						// ����ȷ����Ϣ
						ServerAndCilentUtil.sendConfirmInfo(Room.CONFIRM_WIN, socket);

						Judger.winner = BlackPiece.BLACK;
						break;
					}

					g.setColor(Color.WHITE);

					PointUtil.removePoint(Integer.parseInt(split[0]), Integer.parseInt(split[1]));

					room.setRegret(false);

					WhitePiece.isDown = false; // �׷����Ϊδ��
					BlackPiece.isDown = true;
				}

			}
		} catch (Exception e) {
		} finally {
			if (Judger.escaper == BlackPiece.BLACK) {
				JOptionPane.showMessageDialog(room, "�����˳��˷���!������ʤ��");

			}

			if (Judger.peace == true) {
				JOptionPane.showMessageDialog(room, "����!");
			}

			if (Judger.winner == BlackPiece.BLACK && Judger.escaper != WhitePiece.WHITE) {
				JOptionPane.showMessageDialog(room, "������!");

			}
			
			if (Judger.timeOver == WhitePiece.WHITE) {
				JOptionPane.showMessageDialog(room, "ʱ���þ�!������!");
			}
			
			if (Judger.timeOver == BlackPiece.BLACK) {
				JOptionPane.showMessageDialog(room, "�Է�ʱ���þ�!������ʤ��!");
			}

			if (Judger.winner == WhitePiece.WHITE) {
				if (surrender) {
					JOptionPane.showMessageDialog(room, "�ڷ�Ͷ��,������ʤ��!");
				} else {
					JOptionPane.showMessageDialog(room, "������ʤ��!");

				}
			}

			room.setVisible(false);
			CloseResource.free(room);

			new Game();
		}
	}

}
