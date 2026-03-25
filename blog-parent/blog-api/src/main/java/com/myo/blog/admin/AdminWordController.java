package com.myo.blog.admin;

import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

@RestController
@RequestMapping("/admin/word")
public class AdminWordController {

    /**
     * 接收一个 Word 文件，在"盖章处"关键字所在段落插入印章图片，
     * 并将图片设置为悬浮模式（不挤压正文），最后把处理后的文件返回给浏览器下载。
     *
     * @param file     前端上传的 Word 文件（.docx）
     * @param response HTTP 响应，用于直接将文件写回浏览器
     */
    @PostMapping("/stamp")
    public void stampWord(MultipartFile file, HttpServletResponse response) throws Exception {

        // ========== 第一步：读取上传的 Word 文件 ==========
        // 用 Apache POI 的 XWPFDocument 解析 .docx 文件流
        XWPFDocument document = new XWPFDocument(file.getInputStream());

        // ========== 第二步：遍历段落，定位"盖章处"关键字 ==========
        String keyword = "盖章处";
        XWPFParagraph targetParagraph = null; // 用于存放找到的目标段落

        for (XWPFParagraph paragraph : document.getParagraphs()) {
            if (paragraph.getText().contains(keyword)) {
                targetParagraph = paragraph; // 找到第一个包含关键字的段落即停止
                break;
            }
        }

        // ========== 第三步：在目标段落中插入印章图片 ==========
        if (targetParagraph != null) {
            // 在目标段落末尾新建一个 Run（Run 是 Word 中最小的文字/图片容器）
            XWPFRun run = targetParagraph.createRun();

            // 从本地读取印章图片（测试时需要在 C:\test\ 放一张 stamp.png）
            InputStream picStream = new FileInputStream("C:\\test\\stamp.png");

            // 将图片尺寸从"点（pt）"转换为 Word 内部使用的 EMU 单位（1cm ≈ 914400 EMU）
            // Units.toEMU(120) 表示图片宽高均为 120 点
            int width  = Units.toEMU(120);
            int height = Units.toEMU(120);

            // 以内联（inline）方式把图片插入到 Run 中
            run.addPicture(picStream, XWPFDocument.PICTURE_TYPE_PNG, "stamp", width, height);
            picStream.close();

            // ========== 第四步：将图片从"嵌入"改为"悬浮"，避免挤压正文排版 ==========

            // 获取刚插入图片所在的底层 XML drawing 节点
            org.openxmlformats.schemas.wordprocessingml.x2006.main.CTDrawing drawing =
                    run.getCTR().getDrawingArray(0);

            // 获取当前嵌入式（inline）图片节点
            org.openxmlformats.schemas.drawingml.x2006.wordprocessingDrawing.CTInline inline =
                    drawing.getInlineArray(0);

            // 从 inline 节点中取出图片的图形对象（包含实际图片数据引用）
            org.openxmlformats.schemas.drawingml.x2006.main.CTGraphicalObject graphic =
                    inline.getGraphic();

            // 从 inline 节点中取出图片的宽高尺寸（EMU 单位）
            org.openxmlformats.schemas.drawingml.x2006.main.CTPositiveSize2D extent =
                    inline.getExtent();

            // 保存 inline 节点的文档属性 ID 和 Name，后续赋给 anchor（同一文档内 ID 必须唯一）
            long   docPrId   = inline.getDocPr().getId();
            String docPrName = inline.getDocPr().getName();

            // 在 drawing 节点下新建一个 anchor 节点（anchor = 悬浮图片）
            org.openxmlformats.schemas.drawingml.x2006.wordprocessingDrawing.CTAnchor anchor =
                    drawing.addNewAnchor();

            // 把原来的图形数据和尺寸迁移到 anchor 上（保证图片内容不丢失）
            anchor.setGraphic(graphic);
            anchor.setExtent(extent);

            // 为 anchor 创建文档属性节点，并恢复原来的 ID 和名称（避免 ID 冲突）
            anchor.addNewDocPr();
            anchor.getDocPr().setId(docPrId);
            anchor.getDocPr().setName(docPrName);

            // 设置悬浮行为参数
            anchor.setBehindDoc(false);     // false = 图片浮于文字上方（true 则在文字下方）
            anchor.setLocked(false);        // 允许用户在 Word 里移动图片
            anchor.setLayoutInCell(true);   // 允许图片在表格单元格内定位
            anchor.setAllowOverlap(true);   // 允许与其他悬浮对象重叠

            // 设置水平定位：相对于"字符"位置，偏移量为 0（即紧贴关键字所在位置）
            anchor.addNewPositionH()
                    .setRelativeFrom(
                            org.openxmlformats.schemas.drawingml.x2006.wordprocessingDrawing.STRelFromH.CHARACTER
                    );
            anchor.getPositionH().setPosOffset(0);

            // 设置垂直定位：相对于"行"位置，偏移量为 0（即与当前行顶部对齐）
            anchor.addNewPositionV()
                    .setRelativeFrom(
                            org.openxmlformats.schemas.drawingml.x2006.wordprocessingDrawing.STRelFromV.LINE
                    );
            anchor.getPositionV().setPosOffset(0);

            // 删除原来的 inline 节点，只保留 anchor（二者只能存其一）
            drawing.removeInline(0);
        }

        // ========== 第五步：将处理完的 Word 文件写入响应流，让浏览器下载 ==========

        // 告诉浏览器返回的是 .docx 文件
        response.setContentType(
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        );
        // 设置下载文件名
        response.setHeader("Content-Disposition", "attachment; filename=stamped_document.docx");

        // 把文档写入响应输出流
        OutputStream out = response.getOutputStream();
        document.write(out);

        // 关闭资源，释放内存
        out.close();
        document.close();
    }
}
