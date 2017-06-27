package com.dartin.net;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ServerMessage implements Serializable {

	private static final long serialVersionUID = -1996443800110847339L;
	private static final byte STOP_BYTE = 13;

	public static final int VERSION = 3;
	public static final int MESSAGE_LENGTH = 4096;

	public static final int CMD_RUN = 0;
	public static final int CMD_ADD = 0x10;
	public static final int CMD_REMOVE = 0x11;
	public static final int CMD_REMOVE_LOWER = 0x12;
	public static final int CMD_VERIFY = 0x20;
	public static final int CMD_RESTORE = 0x21;

	public static final String CONTENT_LOG = "log";
	public static final String CONTENT_SET = "set";
	public static final String CONTENT_VER = "ver";

	private int cmd;
	private Map<String, Object> content;
	private int signature;

	private boolean lock = false;

	public ServerMessage(int cmd) {
		this.cmd = cmd;
		content = new HashMap<>();
		signature = 0;
	}

	public int getCmd() {
		return cmd;
	}

	public Object getContent(String key) {
		return content.get(key);
	}

	public int getSignature() {
		return signature;
	}

	public <E extends Serializable> void addContent(String name, E content) {
		if (!lock) {
			this.content.put(name, content);
		} else {
			throw new IllegalStateException("ServerMessage.content is locked");
		}
	}

	public void lock() {
		lock = true;
		signature = content.hashCode();
	}

	//serialization
	public byte[] toBytes() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(this);
		bos.write(STOP_BYTE);
		return bos.toByteArray();
	}

	//deserialization
	public static ServerMessage recover(byte[] bytes) throws IOException, ClassNotFoundException {
		int i = MESSAGE_LENGTH;
		do i--; while (bytes[i] != STOP_BYTE);
		bytes = Arrays.copyOf(bytes, i);
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		ObjectInputStream ois = new ObjectInputStream(bis);
		return (ServerMessage) ois.readObject();
	}
}

