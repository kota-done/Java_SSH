package com.example.todolist.view;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import org.springframework.web.servlet.view.document.AbstractPdfView;

import com.example.todolist.entity.Task;
import com.example.todolist.entity.Todo;
import com.lowagie.text.Cell;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Table;
import com.lowagie.text.alignment.HorizontalAlignment;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class TodoPdf extends AbstractPdfView {
	@Override
	protected void buildPdfDocument(Map<String, Object> model, Document doc, PdfWriter writer,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		// ゴシック16pt_太字
		Font font_g16_bold = new Font(
				BaseFont.createFont("HeiseiKakuGo-W5", "UniJIS-UCS2-H", BaseFont.NOT_EMBEDDED),
				16,
				Font.BOLD);
		// ゴシック12pt
		Font font_g12 = new Font(
				BaseFont.createFont("HeiseiKakuGo-W5", "UniJIS-UCS2-H", BaseFont.NOT_EMBEDDED),
				12);
		// ゴシック10pt
		Font font_g10 = new Font(
				BaseFont.createFont("HeiseiKakuGo-W5", "UniJIS-UCS2-H", BaseFont.NOT_EMBEDDED),
				10);
		// ゴシック12pt_下線あり
		Font font_g12_underline = new Font(
				BaseFont.createFont("HeiseiKakuGo-W5", "UniJIS-UCS2-H", BaseFont.NOT_EMBEDDED),
				11,
				Font.UNDERLINE);
		// ゴシック10pt_赤
		Font font_g10_red = new Font(
				BaseFont.createFont("HeiseiKakuGo-W5", "UniJIS-UCS2-H", BaseFont.NOT_EMBEDDED),
				10);
		font_g10_red.setColor(new Color(255, 0, 0));

		doc.add(new Paragraph("ToDo List　", font_g16_bold));
		doc.add(new Paragraph("　", font_g16_bold));

		// Todo取得
		int numOfTodo = 0;
		int numOfDoneTodo = 0;
		int numOfTask = 0;
		int numOfDoneTask = 0;

		// Todo明細出力
		@SuppressWarnings("unchecked")
		List<Todo> todoList = (List<Todo>) model.get("todoList");
		for (Todo todo : todoList) {
			++numOfTodo;
			if (todo.getDone().equals("Y")) {
				++numOfDoneTodo;
			}
			doc.add(new Paragraph(todo2paragraph(todo), font_g12));
			for (Task task : todo.getTaskList()) {
				++numOfTask;
				if (task.getDone().equals("Y")) {
					++numOfDoneTask;
				}
				doc.add(new Paragraph("\t" + task2paragraph(task), font_g10));
			}
		}
		// Todoベースの完了率
		int todoDoneRate;
		if (numOfTodo > 0) {
			todoDoneRate = (int) ((float) numOfDoneTodo / (float) numOfTodo * 100);
		} else {
			todoDoneRate = -1;
		}

		// Taskベースの完了率
		int taskDoneRate;
		if (numOfTask > 0) {
			taskDoneRate = (int) ((float) numOfDoneTask / (float) numOfTask * 100);
		} else {
			taskDoneRate = -1;
		}
		// テーブル定義
		doc.add(new Paragraph(" ", font_g12));
		doc.add(new Paragraph("　集計表　", font_g12_underline));

		Table summaryTable = new Table(8);
		summaryTable.setWidth(100);
		summaryTable.setPadding(3);
		//
		// 見出し行
		// Todo数, 完了, 未完了, 完了率, Task数, 完了, 未完了, 完了率
		HorizontalAlignment hcenter = HorizontalAlignment.CENTER;
		Cell cell_11 = makeCell("Todo数", font_g10, 0.9f, hcenter);
		Cell cell_12 = makeCell("完了", font_g10, 0.9f, hcenter);
		Cell cell_13 = makeCell("未完了", font_g10, 0.9f, hcenter);
		Cell cell_14 = makeCell("完了率", font_g10, 0.9f, hcenter);
		Cell cell_15 = makeCell("Task数", font_g10, 0.9f, hcenter);
		Cell cell_16 = makeCell("完了", font_g10, 0.9f, hcenter);
		Cell cell_17 = makeCell("未完了", font_g10, 0.9f, hcenter);
		Cell cell_18 = makeCell("完了率", font_g10, 0.9f, hcenter);
		//
		summaryTable.addCell(cell_11);
		summaryTable.addCell(cell_12);
		summaryTable.addCell(cell_13);
		summaryTable.addCell(cell_14);
		summaryTable.addCell(cell_15);
		summaryTable.addCell(cell_16);
		summaryTable.addCell(cell_17);
		summaryTable.addCell(cell_18);

		// 集計結果行
		// Todo数, 完了, 未完了
		Cell cell_21 = makeCell("" + numOfTodo, font_g10, 1.0f, hcenter);
		Cell cell_22 = makeCell("" + numOfDoneTodo, font_g10, 1.0f, hcenter);
		Cell cell_23 = makeCell("" + (numOfTodo - numOfDoneTodo), font_g10, 1.0f, hcenter);
		// Todo完了率
		Cell cell_24;
		if (todoDoneRate < 0) {
			cell_24 = makeCell("-", font_g10_red, 1.0f, hcenter);
		} else {
			cell_24 = makeCell(todoDoneRate + "%", font_g10_red, 1.0f, hcenter);
		}
		// Task数, 完了, 未完了
		Cell cell_25 = makeCell("" + numOfTask, font_g10, 1.0f, hcenter);
		Cell cell_26 = makeCell("" + numOfDoneTask, font_g10, 1.0f, hcenter);
		Cell cell_27 = makeCell("" + (numOfTask - numOfDoneTask), font_g10, 1.0f, hcenter);
		// Task完了率
		Cell cell_28;
		if (taskDoneRate < 0) {
			cell_28 = makeCell("-", font_g10_red, 1.0f, hcenter);
		} else {
			cell_28 = makeCell(taskDoneRate + "%", font_g10_red, 1.0f, hcenter);
		}
		//
		summaryTable.addCell(cell_21);
		summaryTable.addCell(cell_22);
		summaryTable.addCell(cell_23);
		summaryTable.addCell(cell_24);
		summaryTable.addCell(cell_25);
		summaryTable.addCell(cell_26);
		summaryTable.addCell(cell_27);
		summaryTable.addCell(cell_28);

		doc.add(summaryTable);
	}

	// Cell作成ヘルパーメソッド
	private Cell makeCell(String content, Font font, float grayFill,
			HorizontalAlignment alignment) {
		Cell cell = new Cell(new Phrase(content, font));
		cell.setGrayFill(grayFill);
		cell.setHorizontalAlignment(alignment);

		return cell;
	}

	// ToooをPDF出力用に編集して返す
	private String todo2paragraph(Todo todo) {
		StringBuilder sb = new StringBuilder();
		// 完了/未完了
		if (todo.getDone().equals("Y")) {
			sb.append("■");
		} else {
			sb.append("□");
		}
		// 件名
		sb.append(" " + todo.getTitle());
		// 重要度
		sb.append(" ( ");
		sb.append("重要度： " + (todo.getImportance() == 1 ? "★★★" : "★"));
		sb.append(" / ");
		// 緊急度
		sb.append("緊急度： " + (todo.getUrgency() == 1 ? "★★★" : "★"));
		sb.append(" / ");
		// 期限
		sb.append(" 期限： " + (todo.getDeadline() == null ? "-" : todo.getDeadline()));
		sb.append(" )");

		return sb.toString();
	}

	// ToooをPDF出力用に編集して返す
	private String task2paragraph(Task task) {
		StringBuilder sb = new StringBuilder();
		// 完了/未完了
		if (task.getDone().equals("Y")) {
			sb.append("■");
		} else {
			sb.append("□");
		}
		// 件名
		sb.append(" " + task.getTitle());
		// 期限
		sb.append(" ( ");
		sb.append(" 期限： " + (task.getDeadline() == null ? "-" : task.getDeadline()));
		sb.append(" )");

		return sb.toString();
	}
}