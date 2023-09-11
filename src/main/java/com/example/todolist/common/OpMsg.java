package com.example.todolist.common;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OpMsg {
	private String msgType; //”I”:information "W":warninng "E":error
	private String msgText; //メッセージ
}
