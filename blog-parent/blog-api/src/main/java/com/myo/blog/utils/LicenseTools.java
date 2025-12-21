package com.myo.blog.utils;





import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;


/**
 * 许可证获取Macd地址的工具
 */
public class LicenseTools {
    public static void main(String[] args) throws Exception {
        String os = System.getProperty("os.name").toLowerCase();
        System.out.println("您的操作系统是: " + os);
        String mac;
        if (os.startsWith("windows 7")) {
            mac = getWin7MACAddress();
        } else if (os.startsWith("windows")) {
            mac = getWindowsMACAddress();
        } else if (os.startsWith("linux")) {
            mac = getLinuxMACAddress();
        } else if (os.startsWith("mac")) {
            mac = getMacMACAddress();
        } else if (os.startsWith("unix")) {
            mac = getUnixMACAddress();
        } else {
            mac = "";
        }
        System.out.println("您的Mac地址是: " + mac);
    }

    /**
     * 获取Unix网卡的mac地址
     * @return mac地址
     */
    private static String getUnixMACAddress() {
        String mac = null;
        BufferedReader bufferedReader = null;
        Process process = null;
        try {
            // Unix下的命令，一般取eth0作为本地主网卡 显示信息中包含有mac地址信息
            process = Runtime.getRuntime().exec("ifconfig eth0");
            bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = null;
            int index = -1;
            while ((line = bufferedReader.readLine()) != null) {
                // 寻找标示字符串[hwaddr]
                index = line.toLowerCase().indexOf("hwaddr");
                if (index != -1) {
                    // 取出mac地址并去除2边空格
                    mac = line.substring(index + "hwaddr".length() + 1).trim();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            bufferedReader = null;
            process = null;
        }
        return mac;
    }
    /**
     * 获取Linux网卡的mac地址.
     * @return mac地址
     */
    private static String getMacMACAddress() {
        String mac = null;
        BufferedReader bufferedReader = null;
        Process process = null;
        try {
            // linux下的命令，一般取eth0作为本地主网卡 显示信息中包含有mac地址信息
            process = Runtime.getRuntime().exec("ifconfig en0");
            bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = null;
            int index = -1;
            while ((line = bufferedReader.readLine()) != null) {
                index = line.toLowerCase().indexOf("硬件地址");
                if (index != -1) {
                    // 取出mac地址并去除2边空格
                    mac = line.substring(index + "硬件地址".length()).trim();
                    break;
                }

                index = line.toLowerCase().indexOf("ether");
                if (index != -1) {
                    // 取出mac地址并去除2边空格
                    mac = line.substring(index + "ether".length() + 1).trim();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            bufferedReader = null;
            process = null;
        }
        return mac;
    }

    /**
     * 获取Linux网卡的mac地址.
     * @return mac地址
     */
    private static String getLinuxMACAddress() {
        String mac = null;
        BufferedReader bufferedReader = null;
        Process process = null;
        try {
            // linux下的命令，一般取eth0作为本地主网卡 显示信息中包含有mac地址信息
            process = Runtime.getRuntime().exec("ifconfig eth0");
            bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = null;
            int index = -1;
            while ((line = bufferedReader.readLine()) != null) {
                index = line.toLowerCase().indexOf("硬件地址");
                if (index != -1) {
                    // 取出mac地址并去除2边空格
                    mac = line.substring(index + "硬件地址".length()).trim();
                    break;
                }

                index = line.toLowerCase().indexOf("hwaddr");
                if (index != -1) {
                    // 取出mac地址并去除2边空格
                    mac = line.substring(index + "hwaddr".length() + 1).trim();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            bufferedReader = null;
            process = null;
        }
        return mac;
    }

    /**
     * 获取Windows网卡的MAC地址.
     * @return MAC地址
     */
    private static String getWindowsMACAddress() {
        String mac = null;
        BufferedReader bufferedReader = null;
        Process process = null;
        try {
            // windows下的命令，显示信息中包含有mac地址信息
            process = Runtime.getRuntime().exec("ipconfig /all");
            bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            // process = Runtime.getRuntime().exec("cmd.exe /c ipconfig /all");
            // bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream(), "ISO-8859-1"));
            String line = null;
            int index = -1;
            boolean isVMware = false;
            while ((line = bufferedReader.readLine()) != null) {
                // 寻找标示字符串[physical address]
                // 判断是否是VMare虚拟网卡
                if(line.toLowerCase().indexOf("ethernet adapter vmware network adapter") > -1){
                    isVMware = true;
                }

                index = line.toLowerCase().indexOf("physical address");
                if(index == -1)
                    index = line.toLowerCase().indexOf("物理地址");
                if (index != -1) {
                    if(isVMware){
                        isVMware = false;
                        continue;
                    }
                    index = line.indexOf(":");
                    if (index != -1) {
                        // 取出mac地址并去除2边空格
                        mac = line.substring(index + 1).trim();
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            }catch (IOException e1) {
                e1.printStackTrace();
            }
            bufferedReader = null;
            process = null;
        }
        return mac;
    }

    /**
     * Windows 7专用 获取MAC地址
     * @return
     */
    private static String getWin7MACAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            boolean isVMware = false;
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if(ni.isPointToPoint())
                    continue;

                if(ni.getDisplayName().toLowerCase().indexOf("vmware virtual ethernet adapter") > -1)
                    isVMware = true;

                byte[] mac = ni.getHardwareAddress();
                if(null == mac || mac.length == 0)
                    continue;

                if(isVMware){
                    isVMware = false;
                    continue;
                }

                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < mac.length; i++) {
                    if (i != 0)
                        sb.append("-");
                    String s = Integer.toHexString(mac[i] & 0xFF);
                    sb.append(s.length() == 1 ? 0 + s : s);
                }
                return sb.toString().toUpperCase();
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "";
    }
}