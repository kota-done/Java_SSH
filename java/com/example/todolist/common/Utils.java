package com.example.todolist.common;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.LocalDate;

import com.example.todolist.entity.AttachedFile;

public class Utils {
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	/**
	 * 文字列をyyyy-MM-dd型の日付としてjava.sql.Dateオブジェクトへ変換する。<br>
	 * 
	 * @param s 変換対象
	 * @return 変換結果(引数がnull, "", yyyy-MM-dd形式でなければnullを返す)
	 */

	public static Date str2date(String s) {
		if (s == null || s.equals("")) {
			return null;
		}

		long ms = 0;
		try {
			ms = sdf.parse(s).getTime();
		} catch (ParseException e) {
			return null;
		}
		return new Date(ms);
	}

	/**
	 * 文字列をyyyy-MM-dd型の日付としてjava.sql.Dateオブジェクトへ変換する。<br>
	 * 
	 * @param s 変換対象
	 * @return 変換結果(変換できなければnullを返す)
	 */
	public static Date str2dateOrNull(String s) {
		Date date = null;

		try {
			long ms = sdf.parse(s).getTime();
			// yyyy-MM-ddで解釈できた場合
			date = new Date(ms);

		} catch (ParseException e) {
			// 変換できなかった場合
			// date は null のまま
		}

		return date;
	}

	/**
	 * 引数が全角SPACEだけで構成されていればtrueを返す
	 * 
	 * @param s チェック対象
	 * @return true:全角SPACEのみ, "", null false:左記以外
	 */
	public static boolean isAllDoubleSpace(String s) {
		if (s == null || s.equals("")) {
			return true;
		}

		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) != '　') { // 全角SPACE
				return false;
			}
		}
		return true;
	}

	/**
	 * 引数が"" or 半角SPACE/TABだけで構成されているならtrueを返す
	 * 
	 * @param s チェック対象
	 * @return true:半角SPACE/TABのみ or "", null, false:左記以外
	 */
	public static boolean isBlank(String s) {
		if (s == null || s.equals("")) {
			return true;
		}

		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) != ' ' && s.charAt(i) != '\t') {// 半角SPACE
				return false;
			}
		}
		return true;
	}

	/**
	 * 引数がyyyy-mm-ddと解釈できればtrueを返す
	 * 
	 * @param str チェック対象
	 * @return true:yyyy-mm-ddと解釈できる、false:解釈できない or 引数がnull or ""
	 */
	public static boolean isValidDateFormat(String s) {
		if (s == null || s.equals("")) {
			return false;
		}

		try {
			// parseできればyyyy-mm-dd形式とみなす
			LocalDate.parse(s);
			return true;

		} catch (DateTimeException e) {
			// yyyy-mm-dd形式以外
			return false;
		}
	}

	/**
	 * 引数が今日以降の日付を表す文字列(yyyy-MM-dd形式)ならtrueを返す
	 * 
	 * @param str チェック対象
	 * @return true:今日以降、false:昨日以前、引数がyyyy-MM-dd形式でない or null or ""
	 */
	public static boolean isTodayOrFurtureDate(String s) {
		if (s == null || s.equals("")) {
			return false;
		}

		LocalDate tody = LocalDate.now();
		LocalDate deadlineDate = null;
		try {
			// "yyyy-mm-dd" -> LocalDate
			deadlineDate = LocalDate.parse(s);
			if (deadlineDate.isBefore(tody)) {
				// 過去日付なのでfalse
				return false;

			} else {
				return true;
			}

		} catch (DateTimeException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * java.sql.Dateオブジェクトをyyyy-MM-dd形式の文字列へ変換する。<br>
	 * 
	 * @param date 変換対象
	 * @return 変換結果(引数がnullなら""を返す)
	 */
	public static String date2str(Date date) {
		String s = "";
		if (date != null) {
			s = sdf.format(date);
		}

		return s;
	}

	/**
	 * 添付ファイルの格納ファイル名を作成する
	 * 
	 * @param path application.propertiesのattaced.file.path
	 * @param af   添付ファイル情報
	 * @return 格納ファイル名
	 */
	public static String makeAttahcedFilePath(String path, AttachedFile af) {
		return path + "/" + af.getCreateTime() + "_" + af.getFileName();
	}

	/**
	 * ファイル拡張子から対応するContent-Type(MIME Type)を求める
	 * 
	 * @param ext ファイル拡張子
	 * @return MIME Type
	 */
	public static String ext2contentType(String ext) {
		String contentType;
		if (ext == null) {
			return "";
		}

		switch (ext.toLowerCase()) {
		// GIF
		case "gif":
			contentType = "image/gif";
			break;
		// JPEG
		case "jpg":
		case "jpeg":
			contentType = "image/jpeg";
			break;
		// PNG
		case "png":
			contentType = "image/png";
			break;
		// PDF
		case "pdf":
			contentType = "application/pdf";
			break;
		// 上記以外
		default:
			contentType = "";
			break;
		}
		return contentType;
	}

}
