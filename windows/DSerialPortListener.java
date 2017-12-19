import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.TooManyListenersException;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

public class DSerialPortListener implements SerialPortEventListener {
	private String appName = "����ͨѶ";
	private int timeout = 2000;//open �˿�ʱ�ĵȴ�ʱ��

	private CommPortIdentifier commPort;
	private SerialPort serialPort;
	private InputStream inputStream;
	private OutputStream outputStream;
	
	private String portName;	//�˿�����
	private int baudRate;		//������
	private int dataBits=8;		//����λ
	private int stopBit=1;		//ֹͣλ
	private int verifyBit=0;	//����λ
	private String serialHandler; //
	public DSerialPortListener(){}
	public DSerialPortListener(String appName,String portName, int baudRate, int dataBits,
			int stopBit, int verifyBit,String serialHandler) {
		this.appName=appName;
		this.portName = portName.toUpperCase();
		this.baudRate = baudRate;
		this.dataBits = dataBits;
		this.stopBit = stopBit;
		this.verifyBit = verifyBit;
		this.serialHandler=serialHandler;
	}
	
	//���ش��ڵ�ʵ��
	public SerialPort getSerialPort() {
		return serialPort;
	}
	
	/**
	 * @�������� :listPort
	 * @�������� :�г����п��õĴ���
	 * @����ֵ���� :void
	 */
	@SuppressWarnings("rawtypes")
	public static void listPort(){
		CommPortIdentifier cpid;
		Enumeration en = CommPortIdentifier.getPortIdentifiers();
		
		System.out.println("now to list all Port of this PC��" +en);
		
		while(en.hasMoreElements()){
			cpid = (CommPortIdentifier)en.nextElement();
			if(cpid.getPortType() == CommPortIdentifier.PORT_SERIAL){
				System.out.println(cpid.getName() + ", " + cpid.getCurrentOwner());
			}
		}
	}
	
	
	/**
	 * @�������� :selectPort
	 * @�������� :ѡ��һ���˿ڣ����磺COM1
	 * @����ֵ���� :void
	 *	@param portName
	 */
	@SuppressWarnings("rawtypes")
	public void selectPort(){
		
		this.commPort = null;
		CommPortIdentifier cpid;
		Enumeration en = CommPortIdentifier.getPortIdentifiers();
		
		while(en.hasMoreElements()){
			cpid = (CommPortIdentifier)en.nextElement();
			if(cpid.getPortType() == CommPortIdentifier.PORT_SERIAL
					&& cpid.getName().equals(portName)){
				this.commPort = cpid;
				break;
			}
		}
		
		openPort(portName);
	}
	
	/**
	 * @�������� :openPort
	 * @�������� :��SerialPort
	 * @����ֵ���� :void
	 */
	private void openPort(String portName){
		if(commPort == null)
			log(String.format("�޷��ҵ�����Ϊ'%1$s'�Ĵ��ڣ�",portName));
		else{
			log("�˿�ѡ��ɹ�����ǰ�˿ڣ�"+commPort.getName()+",����ʵ���� SerialPort:");
			
			try{
				serialPort = (SerialPort)commPort.open(appName, timeout);
				serialPort.setSerialPortParams(baudRate, dataBits, stopBit, verifyBit);
				log("ʵ�� SerialPort �ɹ���");
			}catch(Exception e){
				throw new RuntimeException(String.format("�˿�'%1$s'����ʹ���У�", 
						commPort.getName()));
			}
		}
	}
	
	/**
	 * @�������� :checkPort
	 * @�������� :���˿��Ƿ���ȷ����
	 * @����ֵ���� :void
	 */
	private void checkPort(){
		if(commPort == null)
			throw new RuntimeException("û��ѡ��˿ڣ���ʹ�� " +
					"selectPort(String portName) ����ѡ��˿�");
		
		if(serialPort == null){
			throw new RuntimeException("SerialPort ������Ч��");
		}
	}
	
	/**
	 * @�������� :write
	 * @�������� :��˿ڷ������ݣ����ڵ��ô˷���ǰ ��ѡ��˿ڣ���ȷ��SerialPort�����򿪣�
	 * @����ֵ���� :void
	 *	@param message
	 */
	public void write(byte[] message) {
		checkPort();
		
		try{
			outputStream = new BufferedOutputStream(serialPort.getOutputStream());
		}catch(IOException e){
			throw new RuntimeException("��ȡ�˿ڵ�OutputStream����"+e.getMessage());
		}
		
		try{
			outputStream.write(message);
			outputStream.flush();
			log("ָ����Ϣ"+Arrays.toString(message)+"���ͳɹ���");
		}catch(IOException e){
			throw new RuntimeException("��˿ڷ�����Ϣʱ����"+e.getMessage());
		}finally{
			try{
				outputStream.close();
			}catch(Exception e){
			}
		}
	}
	
	/**
	 * @�������� :write
	 * @�������� :��˿ڷ������ݣ����ڵ��ô˷���ǰ ��ѡ��˿ڣ���ȷ��SerialPort�����򿪣�
	 * @����ֵ���� :void
	 *	@param message
	 */
	public void writeForSwipeCard(byte[] message) {
		checkPort();
		
		try{
			outputStream = new BufferedOutputStream(serialPort.getOutputStream());
		}catch(IOException e){
			throw new RuntimeException("��ȡ�˿ڵ�OutputStream����"+e.getMessage());
		}
		
		try{
			outputStream.write(message);
			outputStream.flush();
		}catch(IOException e){
			throw new RuntimeException("��˿ڷ�����Ϣʱ����"+e.getMessage());
		}finally{
			try{
				outputStream.close();
			}catch(Exception e){
			}
		}
	}
	
	/**
	 * @�������� :startRead
	 * @�������� :��ʼ�����Ӷ˿��н��յ�����
	 * @����ֵ���� :void
	 *	@param time  ��������Ĵ��ʱ�䣬��λΪ�룬0 ����һֱ����
	 */
	public void startRead(){
		//listPort();
		selectPort();
		checkPort();
		
		try{
			inputStream = new BufferedInputStream(serialPort.getInputStream());
		}catch(IOException e){
			throw new RuntimeException("��ȡ�˿ڵ�InputStream����"+e.getMessage());
		}
		
		try{
			serialPort.addEventListener(this);
		}catch(TooManyListenersException e){
			throw new RuntimeException(e.getMessage());
		}
		
		serialPort.notifyOnDataAvailable(true);
		
		log(String.format("��ʼ��������'%1$s'������--------------", commPort.getName()));
	}
	
	
	/**
	 * @�������� :close
	 * @�������� :�ر� SerialPort
	 * @����ֵ���� :void
	 */
	public void close(){
		serialPort.close();
		serialPort = null;
		commPort = null;
	}
	
	
	public void log(String msg){
		System.out.println(appName+" --> "+msg);
	}


	/**
	 * ���ݽ��յļ���������
	 */
	public void serialEvent(SerialPortEvent arg0) {
		switch(arg0.getEventType()){
		case SerialPortEvent.BI:/*Break interrupt,ͨѶ�ж�*/ 
        case SerialPortEvent.OE:/*Overrun error����λ����*/ 
        case SerialPortEvent.FE:/*Framing error����֡����*/
        case SerialPortEvent.PE:/*Parity error��У�����*/
        case SerialPortEvent.CD:/*Carrier detect���ز����*/
        case SerialPortEvent.CTS:/*Clear to send���������*/ 
        case SerialPortEvent.DSR:/*Data set ready�������豸����*/ 
        case SerialPortEvent.RI:/*Ring indicator������ָʾ*/
        case SerialPortEvent.OUTPUT_BUFFER_EMPTY:/*Output buffer is empty��������������*/ 
            break;
        case SerialPortEvent.DATA_AVAILABLE:/*Data available at the serial port���˿��п������ݡ������������飬������ն�*/
        	byte[] readBuffer = new byte[1024];
        	byte[] dataBuffer = new byte[4096];
        	int totalLength=0;
            
            try {
            	while (inputStream.available() > 0) {
            		//����һ�����ݣ����շֺü��ν��ս������
            		Thread.sleep(200);//Ӳ�������Ƿֶεģ���һ����ʱ������
                    int length = inputStream.read(readBuffer);
                    System.arraycopy(readBuffer, 0, dataBuffer, totalLength, length);
                    totalLength=totalLength+length;
                }
            	/*
            	 * ԭ���Ĵ���
	            log("���յ��˿ڷ�������(����Ϊ"+dataBuffer.length+")��"+Arrays.toString(dataBuffer));
	            
	            //�����������
            	serialHandler.processMsg(dataBuffer);*/
            	
            	/**
            	 * �����������
            	 * add by Win
            	 * ��Ϊ>>>Ѫѹ���ݴ��������, һ����¼��ֶ�δ��䣬����һ�εĲ�����¼������
            	 * ����>>>���Ĵ�����ֽ�����, ��ԭ���̶�4096���ȵ��ֽڸ�Ϊ������Ч�ֽ���������
            	 * ����>>>�����¼������ķ����Ƿ�������ݶ�ʧ, ����
            	 */
            	//System.out.println(">>>>>>���ν������ݳ��ȣ�"+totalLength);
            	byte[] passBytes = new byte[totalLength]; 
            	System.arraycopy(dataBuffer, 0, passBytes, 0, totalLength);
            	//log("���յ��˿ڷ�������(����Ϊ"+passBytes.length+")��"+ Arrays.toString(passBytes));
            	/*
            	 * ������ʵ��������շ�������, ���ڴ������ݽ��� 
            	 */
            	//serialHandler.processMsg(passBytes);
            	//serialHandler.processMsg(passBytes, serialPort);
            } catch (IOException e) {
            } catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	public static void main(String[] args) {
		listPort();
		DSerialPortListener bloodGlucoseHandlerListener=new DSerialPortListener("��������1", "com2", 9600, 8 , 1, 0, null);
    	bloodGlucoseHandlerListener.startRead();
	}
	
	/**
	 * @�������� :�鿴�Ƿ����ָ���˿�
	 * @�������� :ѡ��һ���˿ڣ����磺COM1
	 * @����ֵ���� :void
	 *	@param portName
	 */
	@SuppressWarnings("rawtypes")
	public boolean checkPortAvailable(){
		boolean flag = false;
		this.commPort = null;
		CommPortIdentifier cpid;
		Enumeration en = CommPortIdentifier.getPortIdentifiers();
		
		while(en.hasMoreElements()){
			cpid = (CommPortIdentifier)en.nextElement();
			if(cpid.getPortType() == CommPortIdentifier.PORT_SERIAL
					&& cpid.getName().equals(portName)){
				this.commPort = cpid;
				break;
			}
		}
		
		if(commPort == null) {
			log(String.format("�޷��ҵ�����Ϊ'%1$s'�Ĵ��ڣ�",portName));
			flag = false;
		}
		else{
			log("�˿�ѡ��ɹ�����ǰ�˿ڣ�"+commPort.getName()+",���ڿ���!");
			flag = true;
		}
		return flag;
	}
	
}
