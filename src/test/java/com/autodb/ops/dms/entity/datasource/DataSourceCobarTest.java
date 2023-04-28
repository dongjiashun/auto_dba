package com.autodb.ops.dms.entity.datasource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.util.Map;

/**
 * @author dongjs
 * @since 2016/10/24
 */
public class DataSourceCobarTest {

    ObjectMapper objectMapper = new ObjectMapper();
    @Test
    public void shardings() throws Exception {
        DataSourceCobar cobar = new DataSourceCobar();
        cobar.setMetadata("{\n" +
                "    \"redcliff\": {\n" +
                "        \"slave\": \"rr-bp10rk05pl0hshfz6.mysql.rds.aliyuncs.com:3306:prdquery:5fd7a09a8f6f63519bcd29192c03d03b\",\n" +
                "        \"master\": \"rm-bp115341zi0e9431l.mysql.rds.aliyuncs.com:3306:dbamgr:78eeaf4afc144bbfad7d3434d78e59efc7a52b15a819415e0ab48ca46804e141\"\n" +
                "    },\n" +
                "    \"redcliff\": {\n" +
                "        \"slave\": \"rr-bp189i88h37xbo9f8.mysql.rds.aliyuncs.com:3306:prdquery:b4a66e38b58ce0ecec950921f24fa637\",\n" +
                "        \"master\": \"rm-bp1d3uvz02eu8p801.mysql.rds.aliyuncs.com:3306:dbamgr:c3ea0953d396dd8c77095c18e6f133da5e2790701140633eab7359a568f06996\"\n" +
                "    },\n" +
                "    \"redcliff\": {\n" +
                "        \"slave\": \"rr-bp11e2vjvr40hz809.mysql.rds.aliyuncs.com:3306:prdquery:4bc81e33fd0156b6fe1b5dd76263b6a4\",\n" +
                "        \"master\": \"rm-bp1hx9o2193u94nb6.mysql.rds.aliyuncs.com:3306:dbamgr:fafaa58dfa84ffab58cdf8ebac285e9d8504ee818459e7853d6b855b3a79faff\"\n" +
                "    },\n" +
                "    \"redcliff\": {\n" +
                "        \"slave\": \"rr-bp1c6drraj76634n9.mysql.rds.aliyuncs.com:3306:prdquery:3065d96367690b339df4ef9c4ecdd36d\",\n" +
                "        \"master\": \"rm-bp1rvx4auc33gk1zp.mysql.rds.aliyuncs.com:3306:dbamgr:5d33744bb01cf0b08fa2f0ed11b575e8992c14daf692325c46fa19db349cefdb\"\n" +
                "    },\n" +
                "    \"redcliff\": {\n" +
                "        \"slave\": \"rr-bp11qf62h8jr90j14.mysql.rds.aliyuncs.com:3306:prdquery:5036a5f56ae29950ee551c4aee450437\",\n" +
                "        \"master\": \"rm-bp186f46nxy1u69m1.mysql.rds.aliyuncs.com:3306:dbamgr:1dd4e00d30c811b67eb7c36a16b67b5015a5c585a2f0abed0005aab5f33ba7a6\"\n" +
                "    },\n" +
                "    \"redcliff\": {\n" +
                "        \"slave\": \"rr-bp121244l9o7svxmu.mysql.rds.aliyuncs.com:3306:prdquery:c932cadefa687d5e80d144f96dd3c106\",\n" +
                "        \"master\": \"rm-bp1pc0b944qob7g7o.mysql.rds.aliyuncs.com:3306:dbamgr:124583884eb390077d2e135608e2a2bee8cb1cd8dbc093105934bd92a9f73f1e\"\n" +
                "    },\n" +
                "    \"redcliff\": {\n" +
                "        \"slave\": \"rr-bp18ysoxe0269r9dm.mysql.rds.aliyuncs.com:3306:prdquery:e4669d45fa9d6e5bbb03b3f47dbfb7d4\",\n" +
                "        \"master\": \"rm-bp15s8y8l4nrz037i.mysql.rds.aliyuncs.com:3306:dbamgr:2408503be80d9edd6a57c4ac94661b1f5977da1bbea925d9ff1871381aea9c7f\"\n" +
                "    },\n" +
                "    \"redcliff\": {\n" +
                "        \"slave\": \"rr-bp16x033ytvbv1z0m.mysql.rds.aliyuncs.com:3306:prdquery:456fde8e9eb8495261c1b6e002d2ef2f\",\n" +
                "        \"master\": \"rm-bp1t191tcq31y8vj7.mysql.rds.aliyuncs.com:3306:dbamgr:252ab4e0a6303e872268317f64cf058b776d4b45bb8e073d3088d171969b9b54\"\n" +
                "    }\n" +
                "}");
        cobar.shardings().forEach(System.out::println);

        String meta = "{\n" +
                "    \"redcliff\": {\n" +
                "        \"slave\": \"rr-bp10rk05pl0hshfz6.mysql.rds.aliyuncs.com:3306:prdquery:5fd7a09a8f6f63519bcd29192c03d03b\",\n" +
                "        \"master\": \"rm-bp115341zi0e9431l.mysql.rds.aliyuncs.com:3306:dbamgr:78eeaf4afc144bbfad7d3434d78e59efc7a52b15a819415e0ab48ca46804e141\"\n" +
                "    },\n" +
                "    \"redcliff\": {\n" +
                "        \"slave\": \"rr-bp189i88h37xbo9f8.mysql.rds.aliyuncs.com:3306:prdquery:b4a66e38b58ce0ecec950921f24fa637\",\n" +
                "        \"master\": \"rm-bp1d3uvz02eu8p801.mysql.rds.aliyuncs.com:3306:dbamgr:c3ea0953d396dd8c77095c18e6f133da5e2790701140633eab7359a568f06996\"\n" +
                "    },\n" +
                "    \"redcliff\": {\n" +
                "        \"slave\": \"rr-bp11e2vjvr40hz809.mysql.rds.aliyuncs.com:3306:prdquery:4bc81e33fd0156b6fe1b5dd76263b6a4\",\n" +
                "        \"master\": \"rm-bp1hx9o2193u94nb6.mysql.rds.aliyuncs.com:3306:dbamgr:fafaa58dfa84ffab58cdf8ebac285e9d8504ee818459e7853d6b855b3a79faff\"\n" +
                "    },\n" +
                "    \"redcliff\": {\n" +
                "        \"slave\": \"rr-bp1c6drraj76634n9.mysql.rds.aliyuncs.com:3306:prdquery:3065d96367690b339df4ef9c4ecdd36d\",\n" +
                "        \"master\": \"rm-bp1rvx4auc33gk1zp.mysql.rds.aliyuncs.com:3306:dbamgr:5d33744bb01cf0b08fa2f0ed11b575e8992c14daf692325c46fa19db349cefdb\"\n" +
                "    },\n" +
                "    \"redcliff\": {\n" +
                "        \"slave\": \"rr-bp11qf62h8jr90j14.mysql.rds.aliyuncs.com:3306:prdquery:5036a5f56ae29950ee551c4aee450437\",\n" +
                "        \"master\": \"rm-bp186f46nxy1u69m1.mysql.rds.aliyuncs.com:3306:dbamgr:1dd4e00d30c811b67eb7c36a16b67b5015a5c585a2f0abed0005aab5f33ba7a6\"\n" +
                "    },\n" +
                "    \"redcliff\": {\n" +
                "        \"slave\": \"rr-bp121244l9o7svxmu.mysql.rds.aliyuncs.com:3306:prdquery:c932cadefa687d5e80d144f96dd3c106\",\n" +
                "        \"master\": \"rm-bp1pc0b944qob7g7o.mysql.rds.aliyuncs.com:3306:dbamgr:124583884eb390077d2e135608e2a2bee8cb1cd8dbc093105934bd92a9f73f1e\"\n" +
                "    },\n" +
                "    \"redcliff\": {\n" +
                "        \"slave\": \"rr-bp18ysoxe0269r9dm.mysql.rds.aliyuncs.com:3306:prdquery:e4669d45fa9d6e5bbb03b3f47dbfb7d4\",\n" +
                "        \"master\": \"rm-bp15s8y8l4nrz037i.mysql.rds.aliyuncs.com:3306:dbamgr:2408503be80d9edd6a57c4ac94661b1f5977da1bbea925d9ff1871381aea9c7f\"\n" +
                "    },\n" +
                "    \"redcliff\": {\n" +
                "        \"slave\": \"rr-bp16x033ytvbv1z0m.mysql.rds.aliyuncs.com:3306:prdquery:456fde8e9eb8495261c1b6e002d2ef2f\",\n" +
                "        \"master\": \"rm-bp1t191tcq31y8vj7.mysql.rds.aliyuncs.com:3306:dbamgr:252ab4e0a6303e872268317f64cf058b776d4b45bb8e073d3088d171969b9b54\"\n" +
                "    }\n" +
                "}";

        String meta1 = "{\n" +
                "    \"shard1\": {\n" +
                "        \"slave\": \"rr-bp14a51e1wxtd4385.mysql.rds.aliyuncs.com:3306:prdquery:2e45212dd07ee9b7d6c586703434f5e8\",\n" +
                "        \"master\": \"rm-bp18ehy93faqhlcsb.mysql.rds.aliyuncs.com:3306:dbamgr:bb978fbd1324a4eba87c51d44454a3728d86941983b787166fd4028f04540839\"\n" +
                "    },\n" +
                "     \"shard2\": {\n" +
                "        \"slave\": \"rr-bp14a51e1wxtd4385.mysql.rds.aliyuncs.com:3306:prdquery:2e45212dd07ee9b7d6c586703434f5e8\",\n" +
                "        \"master\": \"rm-bp18ehy93faqhlcsb.mysql.rds.aliyuncs.com:3306:dbamgr:bb978fbd1324a4eba87c51d44454a3728d86941983b787166fd4028f04540839\"\n" +
                "    },\n" +
                "\t    \"shard3\": {\n" +
                "        \"slave\": \"rr-bp14a51e1wxtd4385.mysql.rds.aliyuncs.com:3306:prdquery:2e45212dd07ee9b7d6c586703434f5e8\",\n" +
                "        \"master\": \"rm-bp18ehy93faqhlcsb.mysql.rds.aliyuncs.com:3306:dbamgr:bb978fbd1324a4eba87c51d44454a3728d86941983b787166fd4028f04540839\"\n" +
                "    },\n" +
                "\t    \"shard4\": {\n" +
                "        \"slave\": \"rr-bp14a51e1wxtd4385.mysql.rds.aliyuncs.com:3306:prdquery:2e45212dd07ee9b7d6c586703434f5e8\",\n" +
                "        \"master\": \"rm-bp18ehy93faqhlcsb.mysql.rds.aliyuncs.com:3306:dbamgr:bb978fbd1324a4eba87c51d44454a3728d86941983b787166fd4028f04540839\"\n" +
                "    },\n" +
                "\t    \"shard5\": {\n" +
                "        \"slave\": \"rr-bp14a51e1wxtd4385.mysql.rds.aliyuncs.com:3306:prdquery:2e45212dd07ee9b7d6c586703434f5e8\",\n" +
                "        \"master\": \"rm-bp18ehy93faqhlcsb.mysql.rds.aliyuncs.com:3306:dbamgr:bb978fbd1324a4eba87c51d44454a3728d86941983b787166fd4028f04540839\"\n" +
                "    },\n" +
                "\t    \"shard6\": {\n" +
                "        \"slave\": \"rr-bp14a51e1wxtd4385.mysql.rds.aliyuncs.com:3306:prdquery:2e45212dd07ee9b7d6c586703434f5e8\",\n" +
                "        \"master\": \"rm-bp18ehy93faqhlcsb.mysql.rds.aliyuncs.com:3306:dbamgr:bb978fbd1324a4eba87c51d44454a3728d86941983b787166fd4028f04540839\"\n" +
                "    },\n" +
                "\t    \"shard7\": {\n" +
                "        \"slave\": \"rr-bp14a51e1wxtd4385.mysql.rds.aliyuncs.com:3306:prdquery:2e45212dd07ee9b7d6c586703434f5e8\",\n" +
                "        \"master\": \"rm-bp18ehy93faqhlcsb.mysql.rds.aliyuncs.com:3306:dbamgr:bb978fbd1324a4eba87c51d44454a3728d86941983b787166fd4028f04540839\"\n" +
                "    },\n" +
                "\t   \"shard8\": {\n" +
                "        \"slave\": \"rr-bp14a51e1wxtd4385.mysql.rds.aliyuncs.com:3306:prdquery:2e45212dd07ee9b7d6c586703434f5e8\",\n" +
                "        \"master\": \"rm-bp18ehy93faqhlcsb.mysql.rds.aliyuncs.com:3306:dbamgr:bb978fbd1324a4eba87c51d44454a3728d86941983b787166fd4028f04540839\"\n" +
                "    }\n" +
                "}";
        Map<String, DataSourceCobar.PushSharding> data = objectMapper
                .readValue(meta, new TypeReference<DataSourceCobar.ShardMap<String, DataSourceCobar.PushSharding>>() { });
        System.out.println(data.size());

        Map<String, DataSourceCobar.PushSharding> data1 = objectMapper
                .readValue(meta1, new TypeReference<DataSourceCobar.ShardMap<String, DataSourceCobar.PushSharding>>() { });
        System.out.println(data1.size());
    }
}