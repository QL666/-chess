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

public class WhiteTimer {
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

	public long getPeriod() {
		return period;
	}

	private Timer timer;
	private TimerTask task;

	public WhiteTimer(final Room room) {
		// ��ǩ
		final JLabel[] jls = room.getJls();
		//////////// ��ʱ��
		timer = new Timer();

		task = new TimerTask() {

			@Override
			public void run() {
				if ((BlackPiece.isDown == true && Judger.mover == BlackPiece.BLACK) || 
						(WhitePiece.isDown == false && Judger.mover == WhitePiece.WHITE)) { // ������ʱ
					second++;
					if (second == 60) { // 60��
						second = 0;
						minite++;
					}

					if (minite == 0) {
						jls[5].setText("0��:" + second + "��");

					} else {
						jls[5].setText(minite + "��:" + second + "��");
					}
					
					try {
						if (minite == Room.TIME) { // ��ɫ��ʱ��ľ�
							if (Judger.mover == BlackPiece.BLACK) {
								ServerAndCilentUtil.sendConfirmInfo(Room.TIMEOVER, room.getServer().getSocket());
								
							}
							Judger.timeOver = WhitePiece.WHITE;
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
