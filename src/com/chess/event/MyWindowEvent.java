package com.chess.event;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

import javax.swing.JFrame;

import com.chess.game.Game;
import com.chess.game.Room;
import com.chess.pieces.BlackPiece;
import com.chess.pieces.WhitePiece;
import com.chess.service.Judger;
import com.chess.util.CloseResource;
import com.chess.util.ServerAndCilentUtil;

public class MyWindowEvent extends WindowAdapter {
	private static Socket s; // �������ͻ��˽�����socket
	// ����˵�����serverSocket
	private ServerSocket serverSocket;
	private ServerSocket serverSocket2;
	private Socket socket; // �ͻ��������˽�����socket
	private JFrame room;
	
	public MyWindowEvent(JFrame room) {
		super();
		this.room = room;
	}

	public MyWindowEvent(Socket socket, JFrame room) {
		super();
		this.socket = socket;
		this.room = room;
	}

	public MyWindowEvent(ServerSocket serverSocket, ServerSocket serverSocket2, JFrame room) {
		super();
		this.serverSocket = serverSocket;
		this.serverSocket2 = serverSocket2;
		this.room = room;
	}

	@Override
	public void windowClosing(WindowEvent e) {
		super.windowClosing(e);
		
		if (room instanceof Game) {
			Game.getThreadPool().shutdown(); // �ر��̳߳�
			System.exit(0);
			
		} else {
			try {
				///////////////////////����˴���
				if (serverSocket != null) { // ����˹ر�
					if (Judger.winner == Judger.UNKNOW) { // �ڷ�����
						Judger.escaper = BlackPiece.BLACK; 
						Room r = (Room) room;
						s = r.getServer().getSocket();
						
						if (s != null && !s.isClosed()) {
							ServerAndCilentUtil.sendConfirmInfo(Room.CLOSE, s);
							
							Judger.winner = WhitePiece.WHITE;
							
						}
						
					}
				}
				
				/////////////////////�ͻ��˴���
				if (socket != null) {
					if (Judger.winner == Judger.UNKNOW) { // �׷�����
						Judger.escaper = WhitePiece.WHITE; 
						if (!socket.isClosed()) { // ���ӷ��ر�
							ServerAndCilentUtil.sendConfirmInfo(Room.CLOSE, socket);
							
							Judger.winner = BlackPiece.BLACK;
							
						}
						
					}
					
				}
				
			} catch (IOException e1) {
				System.out.println("windowEvent�����쳣");
			} finally {
				
				CloseResource.close(s);
				CloseResource.close(serverSocket);
				CloseResource.close(serverSocket2);
				CloseResource.close(socket);
				
				room.setVisible(false);
				CloseResource.free(room);
				
				new Game();
			}
			
		}
		

	}

}
