package com.yada.smartpos.spos;

import com.yada.sdk.device.encryption.IEncryption;
import com.yada.sdk.device.encryption.TerminalAuth;
import com.yada.sdk.device.pos.ISequenceGenerator;
import com.yada.sdk.device.pos.IVirtualPos;
import com.yada.sdk.packages.PackagingException;
import com.yada.sdk.packages.transaction.IMessage;
import com.yada.sdk.packages.transaction.IPacker;
import com.yada.smartpos.activity.MainActivity;
import com.yada.smartpos.util.SharedPreferencesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

public class VirtualPos implements IVirtualPos<Traner> {

    private final static Logger LOGGER = LoggerFactory.getLogger(VirtualPos.class);

    private static final String DEFAULT_TELLER_NO = "000";
    private static final String DEFAULT_BATCH_NO = "000000";
    private String merchantId;
    private String terminalId;
    private String tellerNo;
    private String serverIp;
    private int serverPort;
    private int timeout;
    private volatile boolean needSignin = true;
    private volatile boolean needParamDownload = true;
    private TerminalAuth terminalAuth;
    private TerminalParam terminalParam;
    private String batchNo;
    private IPacker packer;
    private ISequenceGenerator traceNoSeqGenerator;
    private ISequenceGenerator cerNoSeqGenerator;
    // 可阻塞的队列
    private LinkedBlockingQueue<IMessage> queue;

    public VirtualPos(String merchantId, String terminalId, IPacker packer,
                      String serverIp, int serverPort, String zmkTmk, int timeout,
                      IEncryption encryptionMachine, MainActivity mainActivity) {
        this(merchantId, terminalId, DEFAULT_TELLER_NO, packer, serverIp, serverPort,
                zmkTmk, timeout, encryptionMachine, mainActivity);
    }

    public VirtualPos(String merchantId, String terminalId, String tellerNo, IPacker packer,
                      String serverIp, int serverPort, String zmkTmk, int timeout,
                      IEncryption encryption, MainActivity mainActivity) {
        this.merchantId = merchantId;
        this.terminalId = terminalId;
        this.tellerNo = tellerNo;
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.timeout = timeout;
        this.terminalAuth = new TerminalAuth(encryption);
        this.terminalParam = new TerminalParam(mainActivity);
        terminalAuth.setTmk(zmkTmk);
        this.batchNo = DEFAULT_BATCH_NO;
        this.packer = packer;
        this.traceNoSeqGenerator = new SequenceGenerator(mainActivity, "traceNo");
        this.cerNoSeqGenerator = new SequenceGenerator(mainActivity, "cerNo");
        this.queue = new LinkedBlockingQueue<IMessage>();
        //加载存储的冲正交易
        load(mainActivity);
        //执行工作线程
        new Thread(new WorkThread()).start();
    }

    @Override
    public Traner createTraner() throws IOException, PackagingException {
        checkSingIn();
        Traner traner = new Traner(merchantId, terminalId, tellerNo, batchNo,
                packer, serverIp, serverPort, timeout, new CheckSignIn(this),
                terminalAuth, traceNoSeqGenerator, cerNoSeqGenerator, queue);
        return traner;
    }

    private synchronized void checkSingIn() throws IOException, PackagingException {
        if (needSignin) {
            Traner traner = new Traner(merchantId, terminalId, tellerNo, batchNo,
                    packer, serverIp, serverPort, timeout, new CheckSignIn(this),
                    terminalAuth, traceNoSeqGenerator, cerNoSeqGenerator, queue);

            SigninInfo si = traner.singIn();
            batchNo = si.batchNo;
            terminalAuth.setTak(si.tmkTak);
            terminalAuth.setTpk(si.tmkTpk);
            traner.close();
            needSignin = false;
        }

        if (needParamDownload) {
            Traner traner = new Traner(merchantId, terminalId, tellerNo, batchNo,
                    packer, serverIp, serverPort, timeout, new CheckSignIn(this),
                    terminalAuth, traceNoSeqGenerator, cerNoSeqGenerator, queue);
            ParamInfo paramInfo = traner.paramDownload();
            terminalParam.setBlock01(paramInfo.getBlock01());
            terminalParam.setAid(paramInfo.getBlock03Map());
            terminalParam.setCAPK(paramInfo.getBlock04_1Map(), paramInfo.getBlock04_2Map());
            traner.close();
            needParamDownload = false;
        }
    }

    public void resetSingIn() {
        needSignin = true;
    }

    public void resetParamDownload() {
        needParamDownload = true;
    }

    public void forward(IMessage t) {
        try {
            Traner traner = createTraner();
            // 发送。
            traner.reversal(t);
        } catch (PackagingException e) {
            LOGGER.debug("when the message forward happen PackagingException", e);
        } catch (IOException e) {
            // 未响应添加至队列中。
            queue.add(t);
        }
    }

    public void store() {

    }

    /**
     * 读取硬盘上的持久化文件，并将内容放入到queue后，删除该文件
     */
    protected void load(MainActivity mainActivity) {
        Set<String> set = SharedPreferencesUtil.getReverseParam(mainActivity);
        for (String message : set) {
            try {
                queue.add(packer.unpack(ByteBuffer.wrap(message.getBytes())));
            } catch (PackagingException e) {
                e.printStackTrace();
            }
        }
        // TODO 持久化文件
    }

    /**
     * 工作线程。
     */
    protected class WorkThread implements Runnable {
        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // 从队列中阻塞式取值
                    IMessage message = queue.take();
                    // 转发
                    forward(message);
                } catch (InterruptedException e) {
                    // 程序停止时，回出现该异常。
                } catch (RuntimeException e) {
                    // TODO LOG 保证工作线程不会因为意外原因退出。
                }
            }
        }
    }
}
