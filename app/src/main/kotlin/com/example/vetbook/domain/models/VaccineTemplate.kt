package com.example.vetbook.domain.models

enum class VaccineSpecies {
    DOG,
    CAT,
}

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

object VaccineTemplates {

    val bySpecies: Map<VaccineSpecies, List<VaccineTemplate>> = mapOf(
        VaccineSpecies.DOG to listOf(
            // ── CORE ──────────────────────────────────────────────────
            VaccineTemplate(
                name = "DHPP #1",
                alsoKnownAs = "Distemper combo",
                type = VaccinationType.CORE,
                offsetDays = 42,
                isRecurring = false,
                intervalDays = null,
                description = "First dose of the core combo. Protects against Distemper, Hepatitis, Parainfluenza, and Parvovirus — all highly contagious and potentially fatal in puppies."
            ),
            VaccineTemplate(
                name = "DHPP #2",
                alsoKnownAs = "Distemper combo",
                type = VaccinationType.CORE,
                offsetDays = 70,
                isRecurring = false,
                intervalDays = null,
                description = "Second dose to boost immunity. Puppies need multiple doses because maternal antibodies can interfere with the first shot."
            ),
            VaccineTemplate(
                name = "DHPP #3",
                alsoKnownAs = "Distemper combo",
                type = VaccinationType.CORE,
                offsetDays = 112,
                isRecurring = false,
                intervalDays = null,
                description = "Final puppy dose. After this the immune system is fully primed. Must be given at 16 weeks or later to be effective."
            ),
            VaccineTemplate(
                name = "DHPP booster",
                alsoKnownAs = "Distemper combo",
                type = VaccinationType.CORE,
                offsetDays = 365,
                isRecurring = true,
                intervalDays = 1095,
                description = "Adult booster every 3 years to maintain long-term immunity against Distemper, Hepatitis, Parainfluenza, and Parvovirus."
            ),
            VaccineTemplate(
                name = "Rabies",
                alsoKnownAs = null,
                type = VaccinationType.CORE,
                offsetDays = 112,
                isRecurring = true,
                intervalDays = 365,
                description = "Required by law in most countries. Rabies is 100% fatal and can spread to humans. A single dose provides strong protection."
            ),
            // ── REGIONAL ─────────────────────────────────────────────
            VaccineTemplate(
                name = "Leptospirosis #1",
                alsoKnownAs = "Lepto 4",
                type = VaccinationType.REGIONAL,
                offsetDays = 84,
                isRecurring = false,
                intervalDays = null,
                description = "First dose of Lepto protection. Leptospirosis is a bacterial infection spread through water and soil contaminated by wildlife urine. Zoonotic — can infect humans too."
            ),
            VaccineTemplate(
                name = "Leptospirosis #2",
                alsoKnownAs = "Lepto 4",
                type = VaccinationType.REGIONAL,
                offsetDays = 112,
                isRecurring = false,
                intervalDays = null,
                description = "Second dose given 3–4 weeks after the first to complete the initial Lepto course."
            ),
            VaccineTemplate(
                name = "Leptospirosis booster",
                alsoKnownAs = "Lepto 4",
                type = VaccinationType.REGIONAL,
                offsetDays = 477,
                isRecurring = true,
                intervalDays = 365,
                description = "Annual booster required — Lepto immunity fades faster than viral vaccines. Especially important for dogs that swim, hike, or live near wildlife."
            ),
            // ── LIFESTYLE ───────────────────────────────────────────
            VaccineTemplate(
                name = "Bordetella",
                alsoKnownAs = "Kennel cough",
                type = VaccinationType.LIFESTYLE,
                offsetDays = 112,
                isRecurring = true,
                intervalDays = 365,
                lifestyleTrigger = "Boarding, kennels, grooming, or dog parks",
                description = "Protects against kennel cough, a highly contagious respiratory infection. Most boarding and grooming facilities require this vaccine. Low risk for dogs that stay home."
            ),
            VaccineTemplate(
                name = "Parainfluenza",
                alsoKnownAs = "CPiV",
                type = VaccinationType.LIFESTYLE,
                offsetDays = 112,
                isRecurring = true,
                intervalDays = 365,
                lifestyleTrigger = "High-contact environments",
                description = "One of the viruses causing kennel cough. Usually given alongside Bordetella as a combined respiratory vaccine for social dogs."
            ),
            VaccineTemplate(
                name = "Canine Flu H3N2",
                alsoKnownAs = "Dog flu",
                type = VaccinationType.LIFESTYLE,
                offsetDays = 112,
                isRecurring = true,
                intervalDays = 365,
                lifestyleTrigger = "Frequent boarding or outbreak regions",
                description = "Protects against the H3N2 strain of canine influenza, common in Asia and parts of the US. Spreads rapidly in kennels and dog parks. Two doses initially, then annual."
            ),
            VaccineTemplate(
                name = "Canine Flu H3N8",
                alsoKnownAs = "Dog flu (H3N8)",
                type = VaccinationType.LIFESTYLE,
                offsetDays = 140,
                isRecurring = true,
                intervalDays = 365,
                lifestyleTrigger = "North America, high-contact environments",
                description = "Protects against the H3N8 strain of canine influenza, more prevalent in North America. Separate from H3N2 — your vet may recommend both if outbreaks are active locally."
            ),
            VaccineTemplate(
                name = "Lyme Disease",
                alsoKnownAs = "Borrelia",
                type = VaccinationType.LIFESTYLE,
                offsetDays = 112,
                isRecurring = true,
                intervalDays = 365,
                lifestyleTrigger = "Tick-endemic regions",
                description = "Protects against Lyme disease transmitted by tick bites. Recommended for dogs that hike, camp, or live in forested or grassy areas with high tick populations."
            ),
            // ── NOT RECOMMENDED ──────────────────────────────────────
            VaccineTemplate(
                name = "Canine Coronavirus",
                alsoKnownAs = "CCoV",
                type = VaccinationType.NOT_RECOMMENDED,
                offsetDays = null,
                isRecurring = false,
                intervalDays = null,
                description = "WSAVA 2024: not recommended. Evidence that CCoV causes serious disease is weak, and the vaccine does not protect against dangerous pantropic strains."
            ),
            VaccineTemplate(
                name = "Giardia",
                alsoKnownAs = "Giardia spp.",
                type = VaccinationType.NOT_RECOMMENDED,
                offsetDays = null,
                isRecurring = false,
                intervalDays = null,
                description = "WSAVA 2024: not recommended. Discontinued in most markets. Insufficient clinical evidence that it prevents infection or reduces shedding."
            ),
        ),
        VaccineSpecies.CAT to listOf(
            // ── CORE ──────────────────────────────────────────────────
            VaccineTemplate(
                name = "FVRCP #1",
                alsoKnownAs = "Cat combo",
                type = VaccinationType.CORE,
                offsetDays = 42,
                isRecurring = false,
                intervalDays = null,
                description = "First dose of the core cat combo. Protects against Rhinotracheitis (herpesvirus), Calicivirus, and Panleukopenia — the three most common and dangerous cat diseases."
            ),
            VaccineTemplate(
                name = "FVRCP #2",
                alsoKnownAs = "Cat combo",
                type = VaccinationType.CORE,
                offsetDays = 70,
                isRecurring = false,
                intervalDays = null,
                description = "Second dose to strengthen immunity. Multiple doses are needed because maternal antibodies in young kittens can block the first shot from working fully."
            ),
            VaccineTemplate(
                name = "FVRCP #3",
                alsoKnownAs = "Cat combo",
                type = VaccinationType.CORE,
                offsetDays = 112,
                isRecurring = false,
                intervalDays = null,
                description = "Final kitten dose. Completes the primary series and establishes long-lasting protection. Should be given at 16 weeks or later."
            ),
            VaccineTemplate(
                name = "FVRCP booster",
                alsoKnownAs = "Cat combo",
                type = VaccinationType.CORE,
                offsetDays = 365,
                isRecurring = true,
                intervalDays = 1095,
                description = "Adult booster every 3 years. Maintains immunity against the three core cat diseases throughout your cat's life."
            ),
            VaccineTemplate(
                name = "Rabies",
                alsoKnownAs = null,
                type = VaccinationType.CORE,
                offsetDays = 112,
                isRecurring = true,
                intervalDays = 365,
                description = "Required by law in most countries. Rabies is always fatal and can be transmitted to humans. Essential even for indoor cats that could escape or encounter bats."
            ),
            // ── REGIONAL ─────────────────────────────────────────────
            VaccineTemplate(
                name = "FeLV #1",
                alsoKnownAs = "Feline leukemia",
                type = VaccinationType.REGIONAL,
                offsetDays = 56,
                isRecurring = false,
                intervalDays = null,
                description = "First dose of FeLV protection. Feline Leukemia Virus weakens the immune system and is a leading cause of cat cancer. Spread through saliva, grooming, and shared bowls."
            ),
            VaccineTemplate(
                name = "FeLV #2",
                alsoKnownAs = "Feline leukemia",
                type = VaccinationType.REGIONAL,
                offsetDays = 84,
                isRecurring = false,
                intervalDays = null,
                description = "Second dose to complete the initial FeLV series. Given 3–4 weeks after the first dose."
            ),
            VaccineTemplate(
                name = "FeLV booster",
                alsoKnownAs = "Feline leukemia",
                type = VaccinationType.REGIONAL,
                offsetDays = 449,
                isRecurring = true,
                intervalDays = 365,
                description = "Annual booster for cats with outdoor access or living with other cats. WSAVA 2024 now considers this core for cats under 1 year and those with outdoor exposure."
            ),
            // ── LIFESTYLE ───────────────────────────────────────────
            VaccineTemplate(
                name = "Bordetella",
                alsoKnownAs = "Kennel cough",
                type = VaccinationType.LIFESTYLE,
                offsetDays = 112,
                isRecurring = true,
                intervalDays = 365,
                lifestyleTrigger = "Multi-cat households, boarding",
                description = "Protects against Bordetella bronchiseptica, a cause of upper respiratory infections in cats. Recommended for cats in multi-cat homes, catteries, or boarding facilities."
            ),
            VaccineTemplate(
                name = "Chlamydia felis",
                alsoKnownAs = "Chlamydophila",
                type = VaccinationType.LIFESTYLE,
                offsetDays = 112,
                isRecurring = true,
                intervalDays = 365,
                lifestyleTrigger = "Catteries or confirmed chlamydial history",
                description = "Protects against Chlamydia felis, a bacterial infection causing chronic eye discharge and respiratory signs. Mainly relevant in catteries or where chlamydia has been confirmed."
            ),
            VaccineTemplate(
                name = "FIV",
                alsoKnownAs = "Feline immunodeficiency",
                type = VaccinationType.LIFESTYLE,
                offsetDays = 112,
                isRecurring = false,
                intervalDays = null,
                lifestyleTrigger = "Outdoor cats in high-prevalence areas",
                description = "Protects against Feline Immunodeficiency Virus (cat HIV). Spread mainly through bite wounds. Note: vaccinated cats will test FIV-positive on standard tests afterward."
            ),
            VaccineTemplate(
                name = "FIP",
                alsoKnownAs = "Feline peritonitis",
                type = VaccinationType.LIFESTYLE,
                offsetDays = 112,
                isRecurring = false,
                intervalDays = null,
                lifestyleTrigger = "Multi-cat households, limited availability",
                description = "Protects against Feline Infectious Peritonitis, a serious coronavirus mutation. Efficacy is debated. Consult your vet — availability is limited in many regions."
            ),
            // ── NOT RECOMMENDED ──────────────────────────────────────
            VaccineTemplate(
                name = "Microsporum canis",
                alsoKnownAs = "Ringworm vaccine",
                type = VaccinationType.NOT_RECOMMENDED,
                offsetDays = null,
                isRecurring = false,
                intervalDays = null,
                description = "WSAVA 2024: not recommended for pets. Insufficient evidence of efficacy. Available in some countries as a therapeutic (treatment) only, not prevention."
            ),
            VaccineTemplate(
                name = "Giardia (feline)",
                alsoKnownAs = "Giardia spp.",
                type = VaccinationType.NOT_RECOMMENDED,
                offsetDays = null,
                isRecurring = false,
                intervalDays = null,
                description = "WSAVA 2024: not recommended. Discontinued globally. No sufficient evidence that it prevents infection or reduces clinical signs in cats."
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