package com.chess.service;

import com.chess.game.ChessBoard;
import com.chess.game.Game;
import com.chess.pieces.BlackPiece;
import com.chess.pieces.WhitePiece;

public class Judger {
	public static final int UNKNOW = -1;
	public static volatile int mover = BlackPiece.BLACK; // �ķ���
	public static volatile int winner = UNKNOW; // ʤ����
	public static volatile int escaper = UNKNOW; // ���ܷ�
	public static volatile boolean peace = false;  // ����
	public static volatile int timeOver = UNKNOW; // ��һ��ʱ��ľ�
	private static int[][] pieces = ChessBoard.getPieces();

	/**
	 * �ж��Ƿ����˻�ʤ
	 * 
	 * @return ��ʤ��
	 */
	public static int judgeWinner() {
		int count = 0;

		for (int i = 0; i < pieces.length; i++) {
			for (int j = 0; j < pieces[i].length; j++) {
				if (pieces[i][j] == BlackPiece.BLACK) { // ����
					count = blackJudgeRight(i, j, BlackPiece.BLACK);
					if (count == Game.WIN_COUNT) {
						return BlackPiece.BLACK;
					}
					
					count = blackJudgeDown(i, j, BlackPiece.BLACK);
					if (count == Game.WIN_COUNT) {
						return BlackPiece.BLACK;
					}

					count = blackJudgeRightDown(i, j, BlackPiece.BLACK);
					if (count == Game.WIN_COUNT) {
						return BlackPiece.BLACK;
					}

					count = blackJudgeLeftDown(i, j, BlackPiece.BLACK);
					if (count == Game.WIN_COUNT) {
						return BlackPiece.BLACK;
					}
					
				} else if (pieces[i][j] == WhitePiece.WHITE) { // ����
					count = blackJudgeRight(i, j, WhitePiece.WHITE);
					if (count == Game.WIN_COUNT) {
						return WhitePiece.WHITE;
					}

					count = blackJudgeDown(i, j, WhitePiece.WHITE);
					if (count == Game.WIN_COUNT) {
						return WhitePiece.WHITE;
					}

					count = blackJudgeRightDown(i, j, WhitePiece.WHITE);
					if (count == Game.WIN_COUNT) {
						return WhitePiece.WHITE;
					}

					count = blackJudgeLeftDown(i, j, WhitePiece.WHITE);
					if (count == Game.WIN_COUNT) {
						return WhitePiece.WHITE;
					}
				}
			}
		}
		
		return UNKNOW;
	}

	/**
	 * ���ұ��ж�
	 * 
	 * @param i
	 * @param j
	 * @return
	 */
	private static int blackJudgeRight(int i, int j, int color) {
		// �ҷ�û����
		if (i == pieces.length)
			return 0;

		// ���Ǻ���
		if (pieces[i][j] != color)
			return 0;

		int count = 1;
		count += blackJudgeRight(++i, j, color);

		return count;
	}

	/**
	 * ���±��ж�
	 * 
	 * @param i
	 * @param j
	 * @return
	 */
	private static int blackJudgeDown(int i, int j, int color) {
		// �·�û����
		if (j == pieces[i].length)
			return 0;

		// ���Ǻ���
		if (pieces[i][j] != color)
			return 0;

		int count = 1;
		count += blackJudgeDown(i, ++j, color);

		return count;
	}

	/**
	 * �����±��ж�
	 * 
	 * @param i
	 * @param j
	 * @return
	 */
	private static int blackJudgeRightDown(int i, int j, int color) {
		// ���·�û����
		if (i == pieces.length || j == pieces[i].length)
			return 0;

		// ���Ǻ���
		if (pieces[i][j] != color)
			return 0;

		int count = 1;

		count += blackJudgeRightDown(++i, ++j, color);

		return count;
	}

	/**
	 * �����±��ж�
	 * 
	 * @param i
	 * @param j
	 * @return
	 */
	private static int blackJudgeLeftDown(int i, int j, int color) {
		// ���·�û����
		if (i == 0 || j == pieces[i].length)
			return 0;

		// ���Ǻ���
		if (pieces[i][j] != color)
			return 0;

		int count = 1;
		count += blackJudgeLeftDown(--i, ++j, color);

		return count;
	}
}
