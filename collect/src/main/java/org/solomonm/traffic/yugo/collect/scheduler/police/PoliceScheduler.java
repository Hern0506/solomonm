package org.solomonm.traffic.yugo.collect.scheduler.police;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;

import org.solomonm.traffic.yugo.collect.global.mapper.YugoMapper;
import org.solomonm.traffic.yugo.collect.global.util.FTPUtil;
import org.solomonm.traffic.yugo.collect.global.vo.DmbAccInciVo;
import org.solomonm.traffic.yugo.collect.scheduler.police.parse.PacketParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@EnableAsync
public class PoliceScheduler {
    
    @Value("${ftp.police.host}")
	String host;

	@Value("${ftp.police.port}")
	int port;

	@Value("${ftp.police.user}")
	String user;

	@Value("${ftp.police.pswd}")
	String pswd;

	@Value("${ftp.police.serverPath}")
	String serverPath;

	@Value("${ftp.police.clientPath}")
	String clientPath;

	@Value("${ftp.police.fileName}")
	String fileName;

	@Autowired
	YugoMapper yugoMapper;

	/**
	 * 경찰정 유고정보 수집 스케줄러
	 */
	@Async
	@Scheduled(cron = "0 */5 * * * *")
	public void run() {
		log.info("Police Yugo Collect Start");

		FTPUtil ftp = null;

		// 1. 경찰청 FTP 접속
		try {
			ftp = new FTPUtil(host, port, user, pswd);
		} catch (Exception e) {
			log.error("FTP Connect Exception Message", e);
		}

		// 2. 유고정보 파일 다운로드
		if (ftp != null) {
			try {
				ftp.downloadFile(clientPath, fileName, serverPath);
			} catch (Exception e) {
				log.error("Yugo File Download Exception Message", e);
			} finally {
				// 경찰정 FTP 접속 종료
				ftp.disconnect();
			}

			// 3. 유고정보 파싱
			List<DmbAccInciVo> saveInfoList = ftpParser();

			// 4. 유고정보 DB Insert
			try {
				if (saveInfoList != null && !saveInfoList.isEmpty() && saveInfoList.size() > 0) {
					yugoMapper.insertInciInfoList(saveInfoList);
				}
			} catch (Exception e) {
				log.error("Yugo Data DB Insert Exception Message", e);
			}
		}
		
		log.info("Police Yugo Collect End");
	}

	public List<DmbAccInciVo> ftpParser() {
		List<DmbAccInciVo> ReturnList = new ArrayList<DmbAccInciVo>();

		BufferedInputStream bis = null;
		FileInputStream fis = null;

		PacketParser pp = new PacketParser();

		Hashtable<String, String> htRes = null;
		HashMap<String, String> StrList = new HashMap<String, String>();
		
		try {
			int readbinary = 0;
			String rcvDATA = "";
			
			File fileTo = new File(clientPath + fileName);
			fis = new FileInputStream(fileTo);

			int fileSize = (int) fileTo.length();
			byte[] readbyte = new byte[fileSize];

			bis = new BufferedInputStream(fis);
			String newLineChar = System.getProperty("line.separator");

			while ((readbinary = bis.read(readbyte, 0, fileSize)) != -1) {
				StringBuffer sb = new StringBuffer("");

				for (int i = 0; i < readbinary; sb.append(readbyte[i] + "|"), i++) {}

				String RcvMsg = DoubleChar(sb.toString().toUpperCase());
				RcvMsg = RcvMsg.replaceAll(newLineChar, "").trim();

				rcvDATA = rcvDATA + RcvMsg;

				if (rcvDATA != null) {
					StrList = byteBufferKr();
					htRes = pp.readPacketIn(StrList, RcvMsg);

					int InciCnt = Integer.parseInt(htRes.get("INCI_TOTAL_CNT"));

					for (int k = 1; k <= InciCnt; k++) {
						log.info("COUNT(" + k + ")" + "INCI_ID(" + htRes.get("INCI_ID" + k) + ")" + "OCCUR_TIME(" + htRes.get("OCCUR_TIME" + k) + ")" + "END_EXPECT_TIME(" + htRes.get("END_EXPECT_TIME" + k) + ")" + "LINKID(" + htRes.get("LINKID" + k) + ")" + "ROAD_CLASS(" + htRes.get("ROAD_CLASS" + k) + ")" + "CONDITION_CODE(" + htRes.get("CONDITION_CODE" + k) + ")" + "INCI_TITLE(" + htRes.get("INCI_TITLE" + k) + ")" + "INCI_CONTENT(" + htRes.get("INCI_CONTENT" + k) + ")" + "XGPS(" + htRes.get("LONGITUDE_X" + k) + ")" + "YGPS(" + htRes.get("LATITUDE_Y" + k) + ")" + "ROAD_NAME(" + htRes.get("ROAD_NAME" + k) + ")");

						DmbAccInciVo tempVo = new DmbAccInciVo();

						tempVo.setInciId(htRes.get("INCI_ID" + k));
						tempVo.setOccurTime(htRes.get("OCCUR_TIME" + k));
						tempVo.setEndExpectTime(htRes.get("END_EXPECT_TIME" + k));
						tempVo.setLinkId(Long.parseLong(htRes.get("LINKID" + k)));
						tempVo.setRoadClass(Integer.parseInt(htRes.get("ROAD_CLASS" + k)));
						tempVo.setConditionCode(conversionConditionCode(htRes.get("CONDITION_CODE" + k)));
						tempVo.setInciTitle(htRes.get("INCI_TITLE" + k));
						tempVo.setInciContent(htRes.get("INCI_CONTENT" + k));
						tempVo.setXgps(Double.parseDouble(htRes.get("LONGITUDE_X" + k)));
						tempVo.setYgps(Double.parseDouble(htRes.get("LATITUDE_Y" + k)));
						tempVo.setRoadName(htRes.get("ROAD_NAME" + k));

						ReturnList.add(tempVo);
					}

					break;
				}
			}
		} catch (Exception e) {
			log.error("ftpParser Exception Message", e);
		}

		return ReturnList;
	}

	private String DoubleChar(String str) {
		StringTokenizer st = new StringTokenizer(str, "|");
		StringBuffer sb = new StringBuffer("");

		while (st.hasMoreTokens()) {
			String strToken = Integer.toHexString(Integer.parseInt(st.nextToken()));

			if (strToken.length() == 1) {
				sb.append("0" + strToken);
			} else if (strToken.length() > 2) {
				sb.append(strToken.substring(strToken.length() - 2));
		 	} else {
				sb.append(strToken);
			}
		}

		return sb.toString().toUpperCase();
	}

	public HashMap<String, String> byteBufferKr() {
		FileInputStream fis = null;
		FileChannel fC = null;
		HashMap<String, String> list = new HashMap<String, String>();

		try {
			File f = new File(clientPath + fileName);
			fis = new FileInputStream(f);
			fC = fis.getChannel();

			ByteBuffer buf = ByteBuffer.allocate((int) f.length());
			
			fC.read(buf);
			fis.close();
			
			PacketParser fP = new PacketParser();
			list = fP.parseStr(buf);
		} catch (Exception e) {
			log.error("byteBufferKr Exception Message", e);
		}

		return list;
	}

	private String conversionConditionCode(String val) {
		String ReturnStr = "";

		switch (val) {
			case "b2":
				ReturnStr = "차량사고";
				break;
			case "b3":
				ReturnStr = "기상사고";
				break;
			case "b4":
				ReturnStr = "차량정지";
				break;
			case "b5":
				ReturnStr = "차량화재";
				break;
			case "b6":
				ReturnStr = "장애물";
				break;
			case "b7":
				ReturnStr = "위험물";
				break;
			case "b8":
				ReturnStr = "지진";
				break;
			case "b9":
				ReturnStr = "산사태";
				break;
			case "b10":
				ReturnStr = "홍수";
				break;
			case "b11":
				ReturnStr = "태풍";
				break;
			case "b12":
				ReturnStr = "시위집회";
				break;
			case "b13":
				ReturnStr = "차량증가";
				break;
			case "b14":
				ReturnStr = "구분없음";
				break;
			default:
				ReturnStr = "Reserved";
				break;
		}

		return ReturnStr;
	}
}
