package com.langhuan.controller;

import com.alibaba.fastjson2.JSONObject;
import com.langhuan.common.Result;
import com.langhuan.model.domain.TRagFileGroup;
import com.langhuan.service.TRagFileGroupService;
import com.langhuan.service.TRagFileService;
import com.langhuan.serviceai.RagService;
import com.langhuan.utils.rag.RagFileVectorUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/rag")
public class RagController {
    private final TRagFileService ragFileService;
    private final TRagFileGroupService ragFileGroupService;
    private final RagFileVectorUtils ragFileVectorUtils;
    private final RagService ragService;

    public RagController(TRagFileService ragFileService, TRagFileGroupService ragFileGroupService, RagFileVectorUtils ragFileVectorUtils, RagService ragService) {
        this.ragFileService = ragFileService;
        this.ragFileGroupService = ragFileGroupService;
        this.ragFileVectorUtils = ragFileVectorUtils;
        this.ragService = ragService;
    }

    @PostMapping("/file-group/add")
    public Result addFileGroup(@Valid @RequestBody TRagFileGroup fileGroup) {
        log.info("Adding new file group: {}", fileGroup);
        if (fileGroup.getGroupName() == null || fileGroup.getGroupType() == null) {
            return Result.error("文件组名称和文件组类型不能为空");
        }
        fileGroup.setCreatedBy(SecurityContextHolder.getContext().getAuthentication().getName());
        boolean success = ragFileGroupService.save(fileGroup);
        return success ? Result.success(fileGroup) : Result.error("添加文件组失败");
    }

    @PostMapping("/file-group/delete")
    public Result deleteFileGroup(@RequestParam Long id) {
        log.info("Deleting file group with ID: {}", id);
        boolean success = ragFileGroupService.removeById(id);
        return success ? Result.success("删除成功") : Result.error("删除文件组失败");
    }

    @PostMapping("/file-group/update")
    public Result updateFileGroup(@Valid @RequestBody TRagFileGroup fileGroup) {
        log.info("Updating file group: {}", fileGroup);
        if (fileGroup.getId() == null) {
            return Result.error("文件组ID不能为空");
        }
        boolean success = ragFileGroupService.updateById(fileGroup);
        return success ? Result.success(fileGroup) : Result.error("更新文件组失败");
    }

    @PostMapping("/file-group/query")
    public Result queryFileGroups(@RequestParam(required = false) String groupName,
                                  @RequestParam(required = false) String groupType,
                                  @RequestParam int pageNum,
                                  @RequestParam int pageSize) {
        log.info("Querying file groups with groupName: {}, groupType: {}, page: {}, size: {}", groupName, groupType, pageNum, pageSize);
        if (groupName == null && groupType == null) {
            return Result.error("至少需要提供文件组名称或文件组类型");
        }
        return Result.success(ragFileGroupService.queryFileGroups(groupName, groupType, pageNum, pageSize));
    }

    @PostMapping("/readAndSplitDocument")
    public Result readAndSplitDocument(
            MultipartFile file,
            String splitFileMethod,
            String methodData
    ) {
        JSONObject jsonObject = JSONObject.parseObject(methodData);
        List<String> list = ragService.readAndSplitDocument(file, splitFileMethod, jsonObject);
        return Result.success(list);
    }

    @PostMapping("/writeDocumentsToVectorStore")
    public Result writeDocumentsToVectorStore(
            @RequestBody String data
//            List<String> documents,
//            String metadata
    ) {
        // 解析 JSON 数据
        JSONObject jsonObjectData = JSONObject.parseObject(data);

        // 获取 documents 并转换为 List<String>
        List<Object> documentsObj = jsonObjectData.getJSONArray("documents");
        List<String> documents = documentsObj.stream()
                .map(Object::toString)
                .toList();

        // 获取 metadata 并转换为 Map<String, Object>
        Map<String, Object> metadata = jsonObjectData.getJSONObject("metadata");

        // 调用服务方法
        return Result.success(ragService.writeDocumentsToVectorStore(documents, metadata));

    }
}
