package com.example.vetbook.domain.models

import androidx.annotation.Keep

@Keep
enum class VaccineSpecies {
    DOG,
    CAT,
}
@Keep
data class VaccineTemplate(
    val name: String,
    val alsoKnownAs: String? = null,
    val type: VaccinationType,
    val offsetDays: Int? = null,
    val isRecurring: Boolean = false,
    val intervalDays: Int? = null,
    val lifestyleTrigger: String? = null,
    val description: String? = null,
)
@Keep
object VaccineTemplates {

    val bySpecies: Map<VaccineSpecies, List<VaccineTemplate>> = mapOf(
        VaccineSpecies.DOG to listOf(
            // ── CORE ──────────────────────────────────────────────────
            VaccineTemplate(
                name = "DHPP #1",
                alsoKnownAs = "Vắc-xin phối hợp Distemper",
                type = VaccinationType.CORE,
                offsetDays = 42,
                isRecurring = false,
                description = "Mũi đầu tiên của bộ vắc-xin cốt lõi. Bảo vệ chó khỏi 4 bệnh nguy hiểm: Distemper (bệnh sài sốt chó), Viêm gan truyền nhiễm (Hepatitis), Viêm phổi-viêm phế quản (Parainfluenza) và Parvovirus gây viêm ruột xuất huyết — đều rất dễ lây lan và có thể gây tử vong ở chó con."
            ),
            VaccineTemplate(
                name = "DHPP #2",
                alsoKnownAs = "Vắc-xin phối hợp Distemper",
                type = VaccinationType.CORE,
                offsetDays = 70,
                isRecurring = false,
                description = "Mũi thứ hai để củng cố miễn dịch. Chó con cần tiêm nhiều mũi vì kháng thể từ mẹ truyền sang có thể cản trở hiệu quả của mũi đầu tiên. Mũi này giúp hệ miễn dịch nhận diện và ghi nhớ mầm bệnh tốt hơn."
            ),
            VaccineTemplate(
                name = "DHPP #3",
                alsoKnownAs = "Vắc-xin phối hợp Distemper",
                type = VaccinationType.CORE,
                offsetDays = 112,
                isRecurring = false,
                description = "Mũi cuối cùng trong loạt tiêm cơ bản. Sau mũi này hệ miễn dịch được kích hoạt đầy đủ và có khả năng bảo vệ lâu dài. Phải tiêm lúc chó được ít nhất 16 tuần tuổi để đạt hiệu quả tối ưu."
            ),
            VaccineTemplate(
                name = "DHPP nhắc lại",
                alsoKnownAs = "Vắc-xin phối hợp Distemper",
                type = VaccinationType.CORE,
                offsetDays = 365,
                isRecurring = true,
                intervalDays = 1095,
                description = "Mũi nhắc lại cho chó trưởng thành, tiêm 3 năm một lần để duy trì miễn dịch bền vững trước Distemper, Viêm gan truyền nhiễm, Parainfluenza và Parvovirus. Không tiêm nhắc lại có thể khiến miễn dịch suy giảm theo thời gian."
            ),
            VaccineTemplate(
                name = "Dại (Rabies)",
                alsoKnownAs = null,
                type = VaccinationType.CORE,
                offsetDays = 112,
                isRecurring = true,
                intervalDays = 365,
                description = "Bắt buộc theo quy định pháp luật tại Việt Nam và hầu hết các quốc gia. Bệnh dại gây tử vong 100% sau khi phát bệnh và có thể lây sang người qua vết cắn. Một mũi tiêm duy nhất đã mang lại khả năng bảo vệ mạnh mẽ. Đây là vắc-xin quan trọng nhất không thể bỏ qua."
            ),
            // ── REGIONAL ─────────────────────────────────────────────
            VaccineTemplate(
                name = "Leptospirosis #1",
                alsoKnownAs = "Lepto 4",
                type = VaccinationType.REGIONAL,
                offsetDays = 84,
                isRecurring = false,
                description = "Mũi đầu tiên phòng bệnh Leptospirosis (xoắn khuẩn Leptospira). Đây là bệnh nhiễm khuẩn lây qua nước và đất bị ô nhiễm bởi nước tiểu của động vật hoang dã. Đặc biệt nguy hiểm vì có thể lây sang người (bệnh zoonotic), gây suy thận và suy gan nếu không điều trị kịp thời."
            ),
            VaccineTemplate(
                name = "Leptospirosis #2",
                alsoKnownAs = "Lepto 4",
                type = VaccinationType.REGIONAL,
                offsetDays = 112,
                isRecurring = false,
                description = "Mũi thứ hai tiêm cách mũi đầu 3–4 tuần để hoàn tất liệu trình cơ bản phòng Leptospirosis. Cần hoàn thành cả hai mũi mới đạt được mức bảo vệ đầy đủ."
            ),
            VaccineTemplate(
                name = "Leptospirosis nhắc lại",
                alsoKnownAs = "Lepto 4",
                type = VaccinationType.REGIONAL,
                offsetDays = 477,
                isRecurring = true,
                intervalDays = 365,
                description = "Nhắc lại hàng năm — miễn dịch Leptospirosis suy giảm nhanh hơn vắc-xin virus. Đặc biệt cần thiết cho chó thường xuyên bơi lội, đi dã ngoại, hay sống gần môi trường có động vật hoang dã. Ở Việt Nam, bệnh này khá phổ biến do khí hậu nhiệt đới ẩm."
            ),
            // ── LIFESTYLE ───────────────────────────────────────────
            VaccineTemplate(
                name = "Bordetella",
                alsoKnownAs = "Ho cũi (Kennel cough)",
                type = VaccinationType.LIFESTYLE,
                offsetDays = 112,
                isRecurring = true,
                intervalDays = 365,
                lifestyleTrigger = "Chó thường xuyên gửi cũi, đi khách sạn thú cưng, cắt tỉa hoặc tiếp xúc nhiều chó khác",
                description = "Bảo vệ chống bệnh ho cũi (viêm phế quản truyền nhiễm), cực kỳ dễ lây trong môi trường tập trung nhiều chó. Hầu hết các cơ sở gửi thú cưng và salon cắt tỉa đều yêu cầu vắc-xin này. Ít cần thiết cho chó ít ra ngoài hoặc không tiếp xúc với chó khác."
            ),
            VaccineTemplate(
                name = "Parainfluenza",
                alsoKnownAs = "CPiV",
                type = VaccinationType.LIFESTYLE,
                offsetDays = 112,
                isRecurring = true,
                intervalDays = 365,
                lifestyleTrigger = "Môi trường đông đúc, tiếp xúc thường xuyên với nhiều chó",
                description = "Một trong những virus gây bệnh ho cũi. Thường được tiêm kết hợp với Bordetella thành vắc-xin hô hấp tổng hợp. Thích hợp cho chó năng động, hay đi công viên hoặc tham gia các hoạt động xã hội."
            ),
            VaccineTemplate(
                name = "Cúm chó H3N2",
                alsoKnownAs = "Dog flu H3N2",
                type = VaccinationType.LIFESTYLE,
                offsetDays = 112,
                isRecurring = true,
                intervalDays = 365,
                lifestyleTrigger = "Chó thường xuyên gửi cũi hoặc sống ở vùng có dịch",
                description = "Bảo vệ chống chủng H3N2 của cúm chó, phổ biến ở châu Á (bao gồm Việt Nam) và một số khu vực Mỹ. Lây lan rất nhanh trong cũi và công viên chó. Cần tiêm 2 mũi ban đầu, sau đó nhắc lại hàng năm."
            ),
            VaccineTemplate(
                name = "Cúm chó H3N8",
                alsoKnownAs = "Dog flu H3N8",
                type = VaccinationType.LIFESTYLE,
                offsetDays = 140,
                isRecurring = true,
                intervalDays = 365,
                lifestyleTrigger = "Chủ yếu Bắc Mỹ, môi trường tiếp xúc cao",
                description = "Bảo vệ chống chủng H3N8 của cúm chó, phổ biến hơn ở Bắc Mỹ. Khác với H3N2 — bác sĩ thú y có thể khuyến nghị cả hai nếu đang có dịch bùng phát tại khu vực bạn sống."
            ),
            VaccineTemplate(
                name = "Bệnh Lyme",
                alsoKnownAs = "Borrelia burgdorferi",
                type = VaccinationType.LIFESTYLE,
                offsetDays = 112,
                isRecurring = true,
                intervalDays = 365,
                lifestyleTrigger = "Chó hay đi rừng, cắm trại hoặc sống ở vùng nhiều ve",
                description = "Bảo vệ chống bệnh Lyme do ve truyền qua vết đốt. Gây đau khớp, sốt và trong trường hợp nặng có thể ảnh hưởng thận. Được khuyến nghị cho chó thường xuyên tiếp xúc với môi trường có cỏ cao, rừng hoặc vùng đất nhiều ve tích."
            ),
            // ── NOT RECOMMENDED ──────────────────────────────────────
            VaccineTemplate(
                name = "Coronavirus chó (CCoV)",
                alsoKnownAs = "CCoV",
                type = VaccinationType.NOT_RECOMMENDED,
                offsetDays = null,
                isRecurring = false,
                description = "WSAVA 2024: Không khuyến nghị. Bằng chứng rằng CCoV gây bệnh nghiêm trọng còn yếu, và vắc-xin này không bảo vệ chống lại các chủng pantropic nguy hiểm hơn. Không nên tiêm khi chưa có chỉ định cụ thể từ bác sĩ thú y."
            ),
            VaccineTemplate(
                name = "Giardia (chó)",
                alsoKnownAs = "Giardia spp.",
                type = VaccinationType.NOT_RECOMMENDED,
                offsetDays = null,
                isRecurring = false,
                description = "WSAVA 2024: Không khuyến nghị. Đã ngừng sản xuất tại hầu hết thị trường. Không có đủ bằng chứng lâm sàng cho thấy vắc-xin này ngăn ngừa nhiễm bệnh hay giảm thải khuẩn ở chó."
            ),
        ),
        VaccineSpecies.CAT to listOf(
            // ── CORE ──────────────────────────────────────────────────
            VaccineTemplate(
                name = "FVRCP #1",
                alsoKnownAs = "Vắc-xin phối hợp mèo",
                type = VaccinationType.CORE,
                offsetDays = 42,
                isRecurring = false,
                description = "Mũi đầu tiên của bộ vắc-xin cốt lõi cho mèo. Bảo vệ đồng thời chống 3 bệnh phổ biến và nguy hiểm nhất: Viêm mũi-khí quản (Rhinotracheitis/Herpesvirus), Calicivirus gây loét miệng và viêm hô hấp, và Panleukopenia (sài sốt mèo) gây tử vong cao ở mèo con."
            ),
            VaccineTemplate(
                name = "FVRCP #2",
                alsoKnownAs = "Vắc-xin phối hợp mèo",
                type = VaccinationType.CORE,
                offsetDays = 70,
                isRecurring = false,
                description = "Mũi thứ hai để tăng cường miễn dịch. Kháng thể từ mẹ truyền sang mèo con có thể vô hiệu hóa mũi đầu tiên, nên cần tiêm nhiều mũi để đảm bảo hệ miễn dịch được kích hoạt đúng cách."
            ),
            VaccineTemplate(
                name = "FVRCP #3",
                alsoKnownAs = "Vắc-xin phối hợp mèo",
                type = VaccinationType.CORE,
                offsetDays = 112,
                isRecurring = false,
                description = "Mũi cuối cùng hoàn tất liệu trình cơ bản, thiết lập nền miễn dịch bền vững lâu dài. Nên tiêm khi mèo đã được ít nhất 16 tuần tuổi để đạt hiệu quả bảo vệ tốt nhất."
            ),
            VaccineTemplate(
                name = "FVRCP nhắc lại",
                alsoKnownAs = "Vắc-xin phối hợp mèo",
                type = VaccinationType.CORE,
                offsetDays = 365,
                isRecurring = true,
                intervalDays = 1095,
                description = "Nhắc lại 3 năm một lần để duy trì khả năng bảo vệ đầy đủ trước ba bệnh nguy hiểm trong suốt cuộc đời của mèo. Không tiêm nhắc lại có thể làm suy giảm miễn dịch theo thời gian."
            ),
            VaccineTemplate(
                name = "Dại (Rabies)",
                alsoKnownAs = null,
                type = VaccinationType.CORE,
                offsetDays = 112,
                isRecurring = true,
                intervalDays = 365,
                description = "Bắt buộc theo quy định pháp luật. Bệnh dại luôn gây tử vong sau khi phát bệnh và có thể lây sang người. Quan trọng ngay cả với mèo nuôi trong nhà vì chúng vẫn có thể thoát ra ngoài hoặc tiếp xúc với dơi mang virus."
            ),
            // ── REGIONAL ─────────────────────────────────────────────
            VaccineTemplate(
                name = "FeLV #1",
                alsoKnownAs = "Bệnh bạch cầu mèo",
                type = VaccinationType.REGIONAL,
                offsetDays = 56,
                isRecurring = false,
                description = "Mũi đầu tiên phòng Feline Leukemia Virus (FeLV). Virus này làm suy yếu hệ miễn dịch, gây ung thư máu và là một trong những nguyên nhân tử vong hàng đầu ở mèo. Lây qua nước bọt khi chải lông cho nhau, dùng chung bát ăn và tiếp xúc gần gũi."
            ),
            VaccineTemplate(
                name = "FeLV #2",
                alsoKnownAs = "Bệnh bạch cầu mèo",
                type = VaccinationType.REGIONAL,
                offsetDays = 84,
                isRecurring = false,
                description = "Mũi thứ hai tiêm cách 3–4 tuần để hoàn tất liệu trình cơ bản FeLV. Bắt buộc phải tiêm đủ cả hai mũi để đạt mức bảo vệ hiệu quả."
            ),
            VaccineTemplate(
                name = "FeLV nhắc lại",
                alsoKnownAs = "Bệnh bạch cầu mèo",
                type = VaccinationType.REGIONAL,
                offsetDays = 449,
                isRecurring = true,
                intervalDays = 365,
                description = "Nhắc lại hàng năm cho mèo có tiếp xúc với bên ngoài hoặc sống cùng nhiều mèo khác. WSAVA 2024 hiện xem đây là vắc-xin cốt lõi cho mèo dưới 1 tuổi và mèo thường xuyên ra ngoài."
            ),
            // ── LIFESTYLE ───────────────────────────────────────────
            VaccineTemplate(
                name = "Bordetella (mèo)",
                alsoKnownAs = "Ho cũi mèo",
                type = VaccinationType.LIFESTYLE,
                offsetDays = 112,
                isRecurring = true,
                intervalDays = 365,
                lifestyleTrigger = "Hộ nuôi nhiều mèo, gửi trọ thú cưng",
                description = "Bảo vệ chống Bordetella bronchiseptica, một nguyên nhân gây nhiễm trùng đường hô hấp trên ở mèo. Được khuyến nghị cho mèo sống trong hộ có nhiều mèo, nhà trọ mèo (cattery) hoặc cơ sở gửi giữ thú cưng."
            ),
            VaccineTemplate(
                name = "Chlamydia felis",
                alsoKnownAs = "Chlamydophila felis",
                type = VaccinationType.LIFESTYLE,
                offsetDays = 112,
                isRecurring = true,
                intervalDays = 365,
                lifestyleTrigger = "Nhà nuôi mèo tập trung (cattery) hoặc nơi đã xác nhận có Chlamydia",
                description = "Bảo vệ chống nhiễm khuẩn Chlamydia felis gây chảy dịch mắt mãn tính và các triệu chứng hô hấp. Chủ yếu cần thiết trong các cattery hoặc ở những nơi đã từng xác nhận có ca nhiễm Chlamydia."
            ),
            VaccineTemplate(
                name = "FIV",
                alsoKnownAs = "HIV mèo (FIV)",
                type = VaccinationType.LIFESTYLE,
                offsetDays = 112,
                isRecurring = false,
                lifestyleTrigger = "Mèo đi ngoài ở khu vực có tỷ lệ FIV cao",
                description = "Bảo vệ chống Feline Immunodeficiency Virus (tương tự HIV ở người). Lây chủ yếu qua vết cắn. Lưu ý quan trọng: mèo đã tiêm sẽ cho kết quả dương tính trên các xét nghiệm FIV tiêu chuẩn về sau — cần thông báo điều này với bác sĩ khi khám."
            ),
            VaccineTemplate(
                name = "FIP",
                alsoKnownAs = "Viêm phúc mạc truyền nhiễm mèo",
                type = VaccinationType.LIFESTYLE,
                offsetDays = 112,
                isRecurring = false,
                lifestyleTrigger = "Hộ nuôi nhiều mèo, khả dụng hạn chế",
                description = "Bảo vệ chống Feline Infectious Peritonitis (FIP), một đột biến nguy hiểm của coronavirus mèo. Hiệu quả của vắc-xin vẫn còn tranh cãi trong cộng đồng thú y. Hãy tham khảo bác sĩ thú y vì vắc-xin này không sẵn có ở nhiều khu vực."
            ),
            // ── NOT RECOMMENDED ──────────────────────────────────────
            VaccineTemplate(
                name = "Microsporum canis",
                alsoKnownAs = "Vắc-xin nấm da (Ringworm)",
                type = VaccinationType.NOT_RECOMMENDED,
                offsetDays = null,
                isRecurring = false,
                description = "WSAVA 2024: Không khuyến nghị cho thú cưng. Bằng chứng hiệu quả không đủ thuyết phục. Tại một số quốc gia chỉ được sử dụng như biện pháp điều trị (không phải phòng ngừa). Không nên tự ý tiêm khi chưa có chỉ định của bác sĩ thú y."
            ),
            VaccineTemplate(
                name = "Giardia (mèo)",
                alsoKnownAs = "Giardia spp.",
                type = VaccinationType.NOT_RECOMMENDED,
                offsetDays = null,
                isRecurring = false,
                description = "WSAVA 2024: Không khuyến nghị. Đã ngừng sản xuất toàn cầu. Không có đủ bằng chứng cho thấy vắc-xin này ngăn ngừa nhiễm bệnh hoặc giảm triệu chứng lâm sàng ở mèo."
            ),
        ),
    )

    fun generatableFor(species: VaccineSpecies): List<VaccineTemplate> =
        bySpecies[species].orEmpty().filter {
            it.type != VaccinationType.NOT_RECOMMENDED && it.offsetDays != null
        }

    fun speciesFromPetType(petType: String): VaccineSpecies? = when (petType.lowercase()) {
        "dog", "chó", "cho" -> VaccineSpecies.DOG
        "cat", "mèo", "meo" -> VaccineSpecies.CAT
        else -> null
    }
}