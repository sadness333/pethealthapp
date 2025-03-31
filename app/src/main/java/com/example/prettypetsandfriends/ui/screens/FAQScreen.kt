package com.example.prettypetsandfriends.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.prettypetsandfriends.data.entities.FAQItem
import com.example.prettypetsandfriends.ui.components.CustomBottomNavigation
import com.example.prettypetsandfriends.ui.components.CustomTopBar

@Composable
fun FAQScreen(navController: NavController) {
    val faqItems = remember {
        mutableStateListOf(
            FAQItem(
                question = "🐾 Как часто нужно посещать ветеринара?",
                answer = "Плановые осмотры рекомендуются:\n\n• Взрослые животные: 1-2 раза в год\n• Щенки/котята: раз в месяц до 6 месяцев\n• Пожилые питомцы: каждые 6 месяцев\n• Хронические больные: по графику врача",
                icon = Icons.Default.MedicalServices
            ),
            FAQItem(
                question = "💉 Обязательна ли вакцинация?",
                answer = "Обязательные прививки:\n\n✅ Бешенство (законодательное требование)\n✅ Чумка плотоядных\n✅ Парвовирусный энтерит\n✅ Ринотрахеит (для кошек)\n\nГрафик: первый курс в 2-4 месяца, ревакцинация ежегодно",
                icon = Icons.Default.Healing
            ),
            FAQItem(
                question = "🚨 Экстренные ситуации",
                answer = "Срочно к врачу при:\n\n• Травмах с кровотечением\n• Затруднённом дыхании\n• Судорогах\n• Отравлении\n• Температуре выше 40°C\n• Отказе от еды/воды более суток",
                icon = Icons.Default.Emergency
            ),
            FAQItem(
                question = "🩺 Подготовка к операции",
                answer = "1. Голодная диета 8-12 часов\n2. Доступ к воде\n3. Гигиеническая обработка\n4. Принести ветеринарный паспорт\n5. Сообщить о принимаемых препаратах",
                icon = Icons.Default.LocalHospital
            ),
            FAQItem(
                question = "❤️ Послеоперационный уход",
                answer = "• Покой 10-14 дней\n• Защитный воротник\n• Обработка швов 2 раза/день\n• Контроль аппетита\n• Ограничение подвижности\n• Контрольный осмотр через 3 дня",
                icon = Icons.Default.Favorite
            ),
            FAQItem(
                question = "🍎 Правильное питание",
                answer = "Основные правила:\n\n• Возрастные линейки кормов\n• Запрет на человеческую еду\n• Свежая вода всегда\n• Дозирование порций\n• Специальные диеты по назначению",
                icon = Icons.Default.Restaurant
            ),
            FAQItem(
                question = "🚗 Путешествия с питомцем",
                answer = "Подготовка:\n\n1. Ветеринарный паспорт\n2. Переноска-контейнер\n3. Аптечка первой помощи\n4. Запас корма\n5. Адаптационные препараты\n6. Чипы/адресник",
                icon = Icons.Default.DirectionsCar
            ),
            FAQItem(
                question = "🐶 Поведенческие проблемы",
                answer = "Частые причины:\n\n• Недостаток активности\n• Неправильная социализация\n• Стрессовые факторы\n• Гормональные изменения\n• Медицинские проблемы\n\nРекомендуем консультацию зоопсихолога",
                icon = Icons.Default.Psychology
            ),
            FAQItem(
                question = "🌡️ Домашняя аптечка",
                answer = "Обязательный набор:\n\n• Перевязочные материалы\n• Антисептики (хлоргексидин)\n• Жаропонижающее\n• Сорбенты\n• Глазные капли\n• Ножницы с тупыми концами\n• Термометр",
                icon = Icons.Default.Medication
            )
        )
    }

    Scaffold(
        topBar = {
            CustomTopBar(
                navController = navController,
                name = "Частые вопросы",
                showBackButton = true,
                onBackClick = { navController.popBackStack() }
            )
        },
        bottomBar = { CustomBottomNavigation(navController) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 12.dp)
        ) {
            item {
                Text(
                    text = "Все что нужно знать о здоровье питомца",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier
                        .padding(vertical = 20.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            items(faqItems) { item ->
                FaqAccordion(item = item) { clickedItem ->
                    val index = faqItems.indexOfFirst { it.question == clickedItem.question }
                    faqItems[index] = faqItems[index].copy(expanded = !faqItems[index].expanded)
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun FaqAccordion(item: FAQItem, onItemClick: (FAQItem) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.animateContentSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onItemClick(item) }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item.icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Text(
                    text = item.question,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = if (item.expanded) Icons.Default.ExpandLess
                    else Icons.Default.ExpandMore,
                    contentDescription = if (item.expanded) "Свернуть" else "Развернуть",
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                )
            }

            if (item.expanded) {
                Divider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Text(
                    text = item.answer,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        lineHeight = 24.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                    ),
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
            }
        }
    }
}