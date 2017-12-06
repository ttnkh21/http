package domain.resp;

import domain.wrap.BaseResp;

/**
 * 时间：2017/12/5 下午4:16
 * data = {} || 只关注data代表的对象<br/>
 * {
 * "code": 1,
 * "msg": "",
 * "data": {
 * "id": "2",
 * "channel": "xiongdi",
 * "vnumber": "9",
 * "url": "http://shangjie888.oss-cn-shanghai.aliyuncs.com/qm28v9.apk",
 * "content": "1.修改升级提示界面效果\n2.修正游客登录cid获取失败问题\n3.修正点击忘记密码去修改密码提交失败问题\n4.修改已知崩溃问题\n5.更改替换部分ui\n6.修改公式失败修正\n",
 * "platform": "android",
 * "force": 0,
 * "name": null,
 * "switch": "1",
 * "versionnum": "1.6.4"
 * }
 * }
 */

public class UpdateResp extends BaseResp {

    public String channel;
    public String id;
    public String vnumber;
    public String versionnum;
    public int force;
    public String name;

    @Override
    public String toString() {
        return "UpdateResp{" +
                "channel='" + channel + '\'' +
                ", id='" + id + '\'' +
                ", vnumber='" + vnumber + '\'' +
                ", versionnum='" + versionnum + '\'' +
                ", force='" + force + '\'' +
                ", name='" + name + '\'' +
                "} " + super.toString();
    }
}
