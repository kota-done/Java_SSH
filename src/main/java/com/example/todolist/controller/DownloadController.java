package com.example.todolist.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.example.todolist.service.DownloadService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class DownloadController {

	public final DownloadService downloadService;

	//添付ファイルのダウンロード処理
	@GetMapping("/todo/af/download/{afId}")
	public void downloadAttachedFile(@PathVariable(name = "afId") int afId, HttpServletResponse response) {
		downloadService.downloadAttachedFile(afId, response);
	}
}
