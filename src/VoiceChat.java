import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;
import java.util.Timer;


/**
 * Created by Still on 16/5/23.
 */
public class VoiceChat {

    //固定发送端口和接收端口,remoteport即对等客户端的接收端口,简化用户操作
    //一台机器上测试时需要修改端口号!
    private static final int sendport=4444;
    private static final int recvport=5555;
    private static final int remoteport=5555;

    private InetAddress remoteaddr;
    private InetAddress localaddr;

    public ConnectGUI cog;
    public ChatInviteGUI cig;
    public ChatGUI chg;

    //分别处理发送和接收服务的两个socket
    public SendSocket ss;
    public ReceiveSocket rs;

    //chatflag=true,通话开始进行;
    //chatflag=false;通话结束;
    private boolean chatflag;

    class ConnectGUI  extends JFrame implements ActionListener {

        //定义界面组件
        private JLabel localip;
        private JLabel remoteip;
        private JLabel status;
        private JTextField li;
        private JTextField ri;
        private JButton connect;
        private JPanel jp1, jp2, jp3;
        private String rip;

        public ConnectGUI() {

            //实例化各组件
            jp1 = new JPanel();
            jp2 = new JPanel();
            jp3 = new JPanel();

            localip=new JLabel("Local IP");
            remoteip = new JLabel("Remote IP");
            status=new JLabel("Not Connected");

            li=new JTextField();
            li.setEnabled(false);
            setLocalip();
            ri = new JTextField();


            connect = new JButton("Connect");
            //为按钮添加事件监听器
            connect.addActionListener(this);
            //为按钮设置command
            connect.setActionCommand("connect");
            connect.setEnabled(true);


            this.add(jp3, BorderLayout.NORTH);
            this.add(jp1, BorderLayout.CENTER);
            this.add(jp2, BorderLayout.SOUTH);
            jp1.setLayout(new GridLayout(2, 2, 5, 5));
            jp1.add(localip);
            jp1.add(li);
            jp1.add(remoteip);
            jp1.add(ri);
            jp2.add(connect);
            jp3.add(status);

            this.setSize(400,150);
            this.setTitle("ONLINE VOICECHAT");
            //设置窗口关闭时操作为结束程序
            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            this.setLocationRelativeTo(null);
            this.setVisible(true);
        }

        public void actionPerformed(ActionEvent e) {
            //获取地址输入
            rip=ri.getText();
            try {
                //根据输入设置remoteaddr
                remoteaddr = InetAddress.getByName(rip);
            }catch (Exception e2){
                System.out.println("error.");
            }
            //通过发送端口发送通话邀请
            ss.sendstatus(20);
            //改变连接界面状态文字
            status.setText("Invitation sent,waiting for accept.");
        }

        public void setStatus(String s){
            //设置status 标签的文字
            status.setText(s);
        }

        public void setLocalip(){
            String ip;
            try {
                //返回本地主机
                InetAddress addr = InetAddress.getLocalHost();
                //返回 IP 地址字符串
                ip = addr.getHostAddress();
            } catch(Exception ex) {
                ip = "";
            }
            //li标签显示本机地址
            li.setText(ip);

        }

    }

    class ChatGUI extends JFrame implements ActionListener{

        private JLabel remoteip,status,chattime,ri,sts,ct;
        private JButton hangup;
        private JPanel jp1,jp2;

        public ChatGUI(String remoteaddr){

            //实例化gui组件
            remoteip=new JLabel("Remote IP");
            status=new JLabel("Status");
            chattime=new JLabel("Chat Time");
            ri=new JLabel(remoteaddr);
            sts=new JLabel("Chatting...");
            ct=new JLabel("0");


            hangup=new JButton("Hang Up");
            //hangup按钮设置事件监听
            hangup.addActionListener(this);
            //hangup按钮设置command
            hangup.setActionCommand("hangup");

            jp1=new JPanel();
            jp2=new JPanel();
            jp1.setLayout(new GridLayout(3,2));
            jp1.add(remoteip);
            jp1.add(ri);
            jp1.add(status);
            jp1.add(sts);
            jp1.add(chattime);
            jp1.add(ct);

            jp2.add(hangup);

            this.add(jp1,BorderLayout.CENTER);
            this.add(jp2,BorderLayout.SOUTH);
            this.setSize(400,200);
            this.setTitle("ONLINE VOICECHAT");
            //设置窗口关闭操作
            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            this.setLocationRelativeTo(null);
            this.setVisible(true);

        }

        public void actionPerformed(ActionEvent e){
            //如果点击hangup按钮
            if(e.getActionCommand().equals("hangup")){
                //设置chatflag,结束通话
                chatflag=false;
                //发送结束通话信息给对等方
                ss.sendstatus(30);
                //关闭通话窗口
                this.dispose();
                //设置连接窗口状态文字
                cog.setStatus("Not Connected.");
                //显示连接窗口
                cog.setVisible(true);
            }
        }

        public void setStatus(String s){
            //设置通话界面状态文字
            sts.setText(s);
        }

        public void setChattime(String t){
            //设置通话时间
            ct.setText(t);
        }

    }

    class ChatInviteGUI extends JFrame implements ActionListener{

        private JLabel rip,inv;
        private JTextField ri;
        private JButton refuse,accept;
        private JPanel jp1, jp2, jp3;

        public ChatInviteGUI(String remoteip){

            //实例化gui组件
            jp1 = new JPanel();
            jp2 = new JPanel();
            jp3 = new JPanel();

            rip=new JLabel("Remote IP");
            inv=new JLabel("Is Inviting You To VoiceChat.");
            ri=new JTextField(remoteip);
            ri.setEnabled(false);

            refuse=new JButton("Refuse");
            accept=new JButton("Accept");
            //refuse按钮设置事件监听
            refuse.addActionListener(this);
            //refuse设置command
            refuse.setActionCommand("refuse");
            //accept按钮设置事件监听
            accept.addActionListener(this);
            //设置command
            accept.setActionCommand("accept");

            jp1.add(rip);
            jp1.add(ri);
            jp2.add(inv);
            jp3.add(refuse);
            jp3.add(accept);

            this.add(jp1,BorderLayout.NORTH);
            this.add(jp2,BorderLayout.CENTER);
            this.add(jp3,BorderLayout.SOUTH);
            this.setSize(400,150);
            this.setTitle("ONLINE VOICECHAT");
            this.setLocationRelativeTo(null);
            this.setVisible(true);



        }


        public void actionPerformed(ActionEvent e){
            //如果点击refuse按钮
            if(e.getActionCommand().equals("refuse")){
                //向邀请发起方发送拒绝信息
                ss.sendstatus(25);
                //关闭邀请窗口
                this.dispose();
            }
            //如果点击accept按钮
            else if(e.getActionCommand().equals("accept")){
                //向邀请发起方发送接受信息
                ss.sendstatus(10);
                //隐藏连接窗口
                cog.setVisible(false);
                //关闭邀请窗口
                this.dispose();
                //实例化通话窗口
                chg=new ChatGUI(remoteaddr.toString());
                //设置chatflag
                chatflag=true;
                //新建线程用于发送语音数据
                Thread t2=new Thread(ss,"send_socket");
                t2.start();
            }
        }



    }

    class SendSocket implements Runnable{

        //定义udp套接字,udp包,接收缓冲区
        private DatagramSocket ss;
        private DatagramPacket packet;
        private byte[] sendbuf;

        public SendSocket(){
            //实例化缓冲区
            sendbuf=new byte[10240];
            try {
                //获取本机地址
                //localaddr = InetAddress.getByName("localhost");
                //实例化udp套接字
                ss = new DatagramSocket(sendport);
            }catch (IOException e){
                System.out.println("error");
                System.exit(-1);
            }
        }


        public void run(){
            //实例化一个通话计时器
            Timer t=new Timer();
            //计时器开始计时
            t.schedule(new TimeCount(),0,1000);
            //实例化Record类
            Record rd=new Record();
            //如果chatflag=true
            while(chatflag){
                //调用Record方法,获取音频数据
                rd.run();
                //讲音频数据放入缓冲区
                sendbuf=rd.getAudioData();
                //讲音频数据打包
                packet=new DatagramPacket(sendbuf,sendbuf.length,remoteaddr,remoteport);
                try {
                    //发送udp包
                    ss.send(packet);
                }catch (IOException e){
                    System.out.println("error");
                    System.exit(-1);
                }
            }
            //关闭Record和计时器
            rd.rClose();
            t.cancel();
        }

        /*调用发送端口发送状态信息
         *status=10:接受通话邀请;
         *status=20:发起通话邀请;
         *status=25:拒绝通话邀请;
         *status=30:结束通话.
         */
        public void sendstatus(int status){

            //将status转化为byte类型
            byte[] res=int2byte(status);
            //Arrays.fill(sendbuf,(byte)0);
            //发送缓冲器前4个字节设置为状态信息
            for(int i=0;i<4;i++){
                sendbuf[i]=res[i];
            }
            //打包数据
            packet=new DatagramPacket(sendbuf,sendbuf.length,remoteaddr,remoteport);
            try {
                //发送udp包
                ss.send(packet);
            }catch (IOException e){
                System.out.println("error");
                System.exit(-1);
            }
        }



    }

    class ReceiveSocket implements Runnable{

        //定义udp套接字,udp包,接收缓冲区
        private DatagramPacket packet;
        private DatagramSocket rs;
        private byte[] recbuf;
        private int dataflag;
        private Play p;

        public  ReceiveSocket(){
            //实例化缓冲区
            recbuf=new byte[10240];
            try{
                //获取本机地址,实例化套接字
                localaddr = InetAddress.getByName("localhost");
                rs=new DatagramSocket(recvport);
            }catch (Exception e){
                System.out.println("error");
                System.exit(-1);
            }
        }

        public void run(){

            //实例化udp包
            packet=new DatagramPacket(recbuf,recbuf.length);
            //实例化Play类
            p=new Play();
            //不断从套接字获取udp包
            while(true) try {
                //接收udp包
                rs.receive(packet);
                //将包数据放入缓冲区
                recbuf = packet.getData();
                //获取udp包的发送地址
                remoteaddr = packet.getAddress();
                //如果不是状态信息且chatflag=true
                if(parseData()&&chatflag) {
                    //播放缓冲区音频数据
                    p.play(recbuf);
                    p.run();
                }
            } catch (IOException e) {
                System.out.println("error");
                System.exit(-1);
            }

        }
        public boolean parseData(){
            //获取remoteaddr
            remoteaddr=packet.getAddress();
            byte[] res=new byte[4];
            //获取包数据前4个字节
            for(int i=0;i<4;i++){
                res[i]=recbuf[i];
            }
            //将前4个字节转化为int类型
            dataflag=byte2int(res);
            //如果是接收邀请
            if(dataflag==10){
                //隐藏连接窗口
                cog.setVisible(false);
                //实例化一个通话窗口
                chg=new ChatGUI(remoteaddr.toString());
                //设置chatflag
                chatflag=true;
                //新建线程用于发送音频数据
                Thread t2=new Thread(ss,"send_socket");
                t2.start();
                return false;
            }
            //如果对方发起通话邀请
            else if(dataflag==20){
                //实例化一个邀请窗口
                cig=new ChatInviteGUI(packet.getAddress().toString());
                return false;
            }
            //如果对方拒绝通话邀请
            else if(dataflag==25){
                //设置连接状态文字
                cog.setStatus("Invitation refused.");
            }
            //如果对方结束通话
            else if(dataflag==30){
                //设置chatflag
                chatflag=false;
                //关闭音频播放
                p.pClose();
                //设置通话窗口状态
                chg.setStatus("Chat ended.");
                return false;
            }
            //如果为音频数据,返回true
            return true;
        }
    }

    //计时器类
    class TimeCount extends TimerTask{
        int t=0;
        @Override
        public void run() {
            //计时+1
            t++;
            //设置通话时间
            chg.setChattime(secToTime(t));
        }
    }

    //实现方法:将秒转化为时分秒
    public static String secToTime(int time) {
        String timeStr = null;
        int hour = 0;
        int minute = 0;
        int second = 0;
        if (time <= 0)
            return "00:00";
        else {
            minute = time / 60;
            if (minute < 60) {
                second = time % 60;
                timeStr = unitFormat(minute) + ":" + unitFormat(second);
            } else {
                hour = minute / 60;
                if (hour > 99)
                    return "99:59:59";
                minute = minute % 60;
                second = time - hour * 3600 - minute * 60;
                timeStr = unitFormat(hour) + ":" + unitFormat(minute) + ":" + unitFormat(second);
            }
        }
        return timeStr;
    }

    public static String unitFormat(int i) {
        String retStr = null;
        if (i >= 0 && i < 10)
            retStr = "0" + Integer.toString(i);
        else
            retStr = "" + i;
        return retStr;
    }

    //VoiceChat构造函数,程序入口
    public VoiceChat(){
        chatflag=false;
        cog=new ConnectGUI();
        ss=new SendSocket();
        rs=new ReceiveSocket();
        Thread t1=new Thread(rs,"receive_socket");
        t1.start();

    }

    //byte转化int类型
    public static int byte2int(byte[] res) {
// 一个byte数据左移24位变成0x??000000，再右移8位变成0x00??0000

        int targets = (res[0] & 0xff) | ((res[1] << 8) & 0xff00) // | 表示安位或
                | ((res[2] << 24) >>> 8) | (res[3] << 24);
        return targets;
    }

    //int转化为byte类型
    public static byte[] int2byte(int res) {
        byte[] targets = new byte[4];

        targets[0] = (byte) (res & 0xff);// 最低位
        targets[1] = (byte) ((res >> 8) & 0xff);// 次低位
        targets[2] = (byte) ((res >> 16) & 0xff);// 次高位
        targets[3] = (byte) (res >>> 24);// 最高位,无符号右移。
        return targets;
    }


    public static void main(String[] args){
        //实例化VoiceChat
        VoiceChat vc=new VoiceChat();
    }

}
