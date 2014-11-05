package com.covidien.gui;

import java.awt.Graphics;
import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class ImagePane extends JPanel {
	private static final long serialVersionUID = 1L;
	private Image image;

	public ImagePane(InputStream stream) throws IOException {
		image = ImageIO.read(stream);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(image, 0, 0, this);
	}
}
