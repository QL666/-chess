package com.chess.game;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JPanel;

import com.chess.pieces.BlackPiece;
import com.chess.service.Judger;

/**
 * ����
 * @author ma
 *
 */
public class ChessBoard extends JPanel {
	// ������
	private static int[][] pieces = new int[Room.BOARD_WIDTH / Room.CRO_LINE_SPACE][Room.HEIGHT / Room.ROW_LINE_SPACE];
	 // �������ѵ���ĵ�
	private static List<Point> blackList = new ArrayList<Point>(20); 
	private static List<Point> whiteList = new ArrayList<Point>(20); 
	
	// �����п��Ե���ĵ�
	private static List<Point> points = new LinkedList<Point>(); 

	public static void init() {
		for (int i = 0; i < pieces.length; i++) {
			for (int j = 0; j < pieces[i].length; j++) {
				pieces[i][j] = 0;
			}
		}
		
		blackList.clear();
		whiteList.clear();
		points.clear();
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		
		int x = 0, y = 0;
		for (y = Room.ROW_LINE_SPACE; y < Room.HEIGHT; y += Room.ROW_LINE_SPACE) { //����,������仯
			for (x = Room.CRO_LINE_SPACE; x < Room.BOARD_WIDTH; x += Room.CRO_LINE_SPACE) {
				if (!Game.connect) {
					points.add(new Point(x, y));
					
				}
				g.drawLine(x, 0, x, Room.HEIGHT);//������
			}
			g.drawLine(0, y, Room.BOARD_WIDTH, y);
		}
		
		for (Point point : blackList) {
			x = (int) point.getX();
			y = (int) point.getY();
			
			g.setColor(Color.BLACK);
				
			g.fillOval(x - 10, y - 10, 20, 20);// �԰뾶��Բ
		}
		
		for (Point point : whiteList) {
			x = (int) point.getX();
			y = (int) point.getY();
			
			g.setColor(Color.WHITE);
				
			g.fillOval(x - 10, y - 10, 20, 20);// �԰뾶��Բ
		}
	}

	public static List<Point> getBlackList() {
		return blackList;
	}

	public static List<Point> getWhiteList() {
		return whiteList;
	}

	public static List<Point> getPoints() {
		return points;
	}

	public static int[][] getPieces() {
		return pieces;
	}

}
