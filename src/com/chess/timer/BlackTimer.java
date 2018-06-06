package com.chess.timer;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JLabel;

import com.chess.game.Room;
import com.chess.pieces.BlackPiece;
import com.chess.pieces.WhitePiece;
import com.chess.service.Judger;
import com.chess.util.ServerAndCilentUtil;

public class BlackTimer {
	private int minite = 0; // ����
	private int second = 0; // ����
	private long delayTime = 1000; // ��ʱ���ӳ�ʱ��
	private final long period = 1000; // ��ʱ����

	public int getMinite() {
		return minite;
	}

	public void setMinite(int minite) {
		this.minite = minite;
	}

	public int getSecond() {
		return second;
	}

	public void setSecond(int second) {
		this.second = second;
	}

	public long getDelayTime() {
		return delayTime;
	}

	public void setDelayTime(long delayTime) {
		this.delayTime = delayTime;
	}

	private Timer timer;
	private TimerTask task;

	public BlackTimer(final Room room) {
		// ��ǩ
		final JLabel[] jls = room.getJls();
		//////////// ��ʱ��
		timer = new Timer();

		task = new TimerTask() {

			@Override
			public void run() {
				if ((BlackPiece.isDown == false && Judger.mover == BlackPiece.BLACK) || 
						(WhitePiece.isDown == true && Judger.mover == WhitePiece.WHITE)) { // ������ʱ
					second++;
					if (second == 60) { // 60��
						second = 0;
						minite++;
						
						
					}

					if (minite == 0) {
						jls[3].setText("0��:" + second + "��");

					} else {
						jls[3].setText(minite + "��:" + second + "��");
					}
					
					try {
						if (minite == Room.TIME) { // ������ʱ��ľ�
							if (Judger.mover == WhitePiece.WHITE) {
								ServerAndCilentUtil.sendConfirmInfo(Room.TIMEOVER, room.getSocket());
								
							}
							Judger.timeOver = BlackPiece.BLACK;
							cancel();
						}
					} catch (IOException e) {
					}
				} 

			}
		};
	}

	public void start() {
		timer.schedule(task, delayTime, period);
	}

	public void cancel() {
		timer.cancel();
	}
}
