package com.example.demo
import java.awt.image.BufferedImage

import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageReader
import javax.imageio.ImageTypeSpecifier
import javax.imageio.ImageWriter
import javax.imageio.metadata.IIOMetadata
import javax.imageio.stream.ImageInputStream
import javax.imageio.stream.ImageOutputStream

import org.dcm4che3.imageio.plugins.dcm.DicomImageReadParam

class DicomTests {

	def getFile(counter) {
		new File("C:\\Users\\sudharshana\\Workspace\\fuji\\samples\\images\\green\\4fca5aec.dcm")
	}

	static void main(String[] args) {
//		(10..19).each {
//			new DicomTests().toJPEG(it)
//		}
		new DicomTests().toJPEG(200)
	}

	def toJPEG(counter) {
		def file = getFile(counter)
		try {
			Iterator<ImageReader> iter = ImageIO.getImageReadersByFormatName("DICOM");
			ImageReader reader = (ImageReader) iter.next();
			DicomImageReadParam param = (DicomImageReadParam) reader.getDefaultReadParam();
			ImageInputStream iis = ImageIO.createImageInputStream(file);
			reader.setInput(iis, false);
			BufferedImage myJpegImage = reader.read(0, param);
			iis.close();
			if (myJpegImage == null) {
				System.out.println("\nError: couldn't read dicom image!");
				return;
			}
			File myJpegFile = new File("C:\\Users\\sudharshana\\Workspace\\fuji\\samples\\DCM\\temp"+counter+".jpg");
			OutputStream output = new BufferedOutputStream(new FileOutputStream(myJpegFile));
			ImageOutputStream ios = ImageIO.createImageOutputStream(output)
			ImageWriter imageWriter = (ImageWriter)ImageIO.getImageWritersBySuffix("jpeg").next();
			imageWriter.setOutput(ios);
			IIOMetadata imageMetaData = imageWriter.getDefaultImageMetadata(new ImageTypeSpecifier(myJpegImage), null);
			imageWriter.write(imageMetaData, new IIOImage(myJpegImage, null, null), null);
			output.close();
		}
		catch(Throwable e) {
			e.printStackTrace()
			System.out.println("\nError: couldn't read dicom image!"+ e.getMessage());
			return;
		}
	}
}