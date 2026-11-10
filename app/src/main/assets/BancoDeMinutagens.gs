/**
 * ============================================================================
 * BANCO DE MINUTAGENS - CÓDIGO DA PLANILHA (Google Apps Script)
 * https://github.com/KalidoraBR/banco-de-minutagens
 *
 * Como usar (o app mostra este passo a passo em Ajustes > Google Sheets):
 *  1. Crie uma planilha em branco no Google Planilhas.
 *  2. Na planilha, abra Extensões > Apps Script, apague o que estiver lá,
 *     cole este código inteiro e salve.
 *  3. Clique em Implantar > Nova implantação > tipo "App da Web".
 *     Executar como: Eu. Quem pode acessar: Qualquer pessoa.
 *     Autorize o acesso quando o Google pedir e copie o link do App da Web.
 *  4. No app, cole o link da planilha e o link do App da Web e toque em Testar.
 *
 * O código cria sozinho uma aba para cada série que o app enviar.
 * As cores das abas aparecem até 1 minuto depois da primeira sincronização.
 * ============================================================================
 */

// Valores aceitos na célula F2 (os mesmos do selo do app)
var STATUS_VALIDOS = ["Em andamento", "Finalizada", "Incompleta", "Em breve"];

function doGet(e) {
  try {
    const action = e && e.parameter ? e.parameter.action : "import_to_app";
    const spreadsheetId = e && e.parameter ? e.parameter.spreadsheetId : null;

    if (action === "import_to_app") {
      return handleImportToApp(spreadsheetId);
    }

    return createJsonResponse({
      success: false,
      error: "Ação inválida para GET."
    });
  } catch (error) {
    return createJsonResponse({
      success: false,
      error: "Erro no servidor: " + error.message
    });
  }
}

function doPost(e) {
  try {
    let payload = {};
    if (e && e.postData && e.postData.contents) {
      payload = JSON.parse(e.postData.contents);
    }

    const action = payload.action || "export_to_sheets";
    const spreadsheetId = payload.spreadsheetId;

    if (action === "clearEpisodes") {
      const ss = getSpreadsheet(spreadsheetId);
      return createJsonResponse(handleClearEpisodes(payload, ss));
    }

    if (action === "export_to_sheets") {
      const resposta = handleExportToSheets(spreadsheetId, payload);
      garantirFormatador_();
      return resposta;
    }

    return createJsonResponse({
      success: false,
      error: "Ação inválida para POST."
    });
  } catch (error) {
    return createJsonResponse({
      success: false,
      error: "Erro ao processar requisição: " + error.message
    });
  }
}

/**
 * ============================================================================
 * 1. FUNÇÃO CENTRAL DE NORMALIZAÇÃO DE TEXTO DE MINUTAGEM
 * ============================================================================
 */
function normalizeTimingText(value) {
  if (value === null || value === undefined) return "";

  let text = String(value).trim();

  if (!text) return "";
  if (text.toLowerCase() === "não possui" || text.toLowerCase() === "nao possui") return "Não possui";

  // Remove apóstrofo inicial se presente
  text = text.replace(/^'/, "");

  // Trata formato de 3 partes com :00 ao final (ex: 40:00:00 -> 40:00, 42:30:00 -> 42:30, 35:00:00 -> 35:00)
  const threeParts = text.match(/^(\d+):(\d{2}):00$/);
  if (threeParts) {
    return threeParts[1].padStart(2, "0") + ":" + threeParts[2];
  }

  // Trata formato de 2 partes (ex: 1:31 -> 01:31, 40:00 -> 40:00)
  const twoParts = text.match(/^(\d+):(\d{2})$/);
  if (twoParts) {
    return twoParts[1].padStart(2, "0") + ":" + twoParts[2];
  }

  return text;
}

/**
 * ============================================================================
 * 2. FUNÇÃO ÚNICA DE ESCRITA DE MINUTAGEM COMO TEXTO SIMPLES (@)
 * ============================================================================
 */
function writeTimingAsText(range, value) {
  const normalized = normalizeTimingText(value);

  if (normalized === "") return false;

  // Limpa o valor anterior para remover qualquer tipo numérico/data já
  // associado à célula. O apóstrofo força texto e não aparece na tela.
  range.clearContent();
  range.setNumberFormat("@");
  range.setValue("'" + normalized);
  return true;
}

/**
 * Devolve o status escrito do jeito oficial, ou "" se não for um dos aceitos.
 */
function normalizeStatus(raw) {
  const text = raw === null || raw === undefined ? "" : String(raw).trim().toLowerCase();
  if (!text) return "";
  for (var i = 0; i < STATUS_VALIDOS.length; i++) {
    if (STATUS_VALIDOS[i].toLowerCase() === text) return STATUS_VALIDOS[i];
  }
  return "";
}

/**
 * ============================================================================
 * 3. IMPORTAÇÃO: PLANILHA -> APP (handleImportToApp)
 * ============================================================================
 */
function handleImportToApp(spreadsheetId) {
  const ss = getSpreadsheet(spreadsheetId);
  const sheets = ss.getSheets();

  const seriesList = [];
  const fixedTimingsList = [];
  const episodesList = [];
  const warnings = [];

  sheets.forEach(sheet => {
    const sheetName = sheet.getName();

    if (sheetName.toLowerCase().startsWith("config") || sheetName.toLowerCase().startsWith("template")) {
      return;
    }
    // aba oculta = desativada (backup, cópia): não entra no app
    if (sheet.isSheetHidden()) return;

    const displayValues = sheet.getDataRange().getDisplayValues();
    if (!displayValues || displayValues.length < 1) return;

    // Layout oficial: B1 TMDB, D1 Ano, F1 IMDb, B2 Título e F2 Status.
    const title = displayValues[1] && displayValues[1][1]
      ? String(displayValues[1][1]).trim()
      : sheetName;
    const rawTmdb = displayValues[0] && displayValues[0][1]
      ? displayValues[0][1]
      : "";
    const tmdbId = normalizeTmdbId(rawTmdb);

    if (!tmdbId) return;

    const releaseYear = displayValues[0] && displayValues[0][3]
      ? parseYearNumber(displayValues[0][3])
      : 0;
    const imdbId = displayValues[0] && displayValues[0][5]
      ? String(displayValues[0][5]).trim()
      : "";
    // F2 vazia ou com valor fora da lista vai como "": o app mantém o selo que já tem.
    const status = displayValues[1] && displayValues[1][5]
      ? normalizeStatus(displayValues[1][5])
      : "";

    seriesList.push({
      tmdbId: tmdbId,
      title: title,
      releaseYear: releaseYear,
      imdbId: imdbId,
      status: status,
      // avisa o app que este status veio da F2 (o código antigo mandava sempre "Em andamento")
      statusDaPlanilha: true
    });

    // Minutagens Fixas na linha 6 (Linha 5: Cabeçalhos, Linha 6: Valores A6:C6)
    if (displayValues.length >= 6) {
      const row6 = displayValues[5];
      const fixOpening = normalizeTimingText(row6[0]);
      const fixRecap = normalizeTimingText(row6[1]);
      const fixEnding = normalizeTimingText(row6[2]);

      fixedTimingsList.push({
        tmdbId: tmdbId,
        opening: fixOpening,
        recap: fixRecap,
        ending: fixEnding
      });
    }

    // Episódios a partir da linha 10 (Índice 9 da matriz)
    if (displayValues.length >= 10) {
      for (let r = 9; r < displayValues.length; r++) {
        const row = displayValues[r];
        const seasonRaw = row[0] ? row[0].trim() : "";
        const episodeRaw = row[1] ? row[1].trim() : "";

        if (!seasonRaw && !episodeRaw) continue;

        const epTitle = row[2] ? row[2].trim() : "";
        const openStart = normalizeTimingText(row[3]);
        const openEnd = normalizeTimingText(row[4]);
        const recapStart = normalizeTimingText(row[5]);
        const recapEnd = normalizeTimingText(row[6]);
        const endStart = normalizeTimingText(row[7]);
        const endEnd = normalizeTimingText(row[8]);

        episodesList.push({
          tmdbId: tmdbId,
          season: seasonRaw,
          number: episodeRaw,
          title: epTitle,
          imdbId: row[9] ? String(row[9]).trim() : "",
          tvdbId: row[10] ? String(row[10]).trim() : "",
          opening: buildTimingObject(openStart, openEnd),
          recap: buildTimingObject(recapStart, recapEnd),
          ending: buildTimingObject(endStart, endEnd)
        });
      }
    }
  });

  return createJsonResponse({
    success: true,
    series: seriesList,
    fixedTimings: fixedTimingsList,
    episodes: episodesList,
    warnings: warnings
  });
}

/**
 * ============================================================================
 * 4. EXPORTAÇÃO: APP -> PLANILHA (handleExportToSheets)
 * ============================================================================
 */
function handleExportToSheets(spreadsheetId, payload) {
  const ss = getSpreadsheet(spreadsheetId);
  const seriesPayload = payload.series || [];

  let seriesProcessed = 0;
  let episodesProcessed = 0;
  const warnings = [];

  seriesPayload.forEach(serie => {
    const tmdbId = normalizeTmdbId(serie.tmdbId || serie.id);
    if (!tmdbId) return;

    let sheet = getSheetByTmdbId(ss, tmdbId);

    // Se a aba não existir, cria uma nova aba com o layout padrão
    if (!sheet) {
      sheet = ss.insertSheet(tmdbId);
      setupNewSheetLayout(sheet, serie);
    }

    // Mantém o layout oficial da planilha.
    sheet.getRange("A1").setValue("TMDB ID:");
    sheet.getRange("B1").setNumberFormat("@").setValue("'" + tmdbId);
    sheet.getRange("C1").setValue("Ano:");
    if (serie.releaseYear) sheet.getRange("D1").setValue(serie.releaseYear);
    sheet.getRange("E1").setValue("IMDb ID:");
    if (serie.imdbId) sheet.getRange("F1").setNumberFormat("@").setValue("'" + serie.imdbId);
    sheet.getRange("A2").setValue("Título:");
    if (serie.title) sheet.getRange("B2").setValue(serie.title);

    // Status da série (selo do app). Valor fora da lista não apaga o que já está na F2.
    sheet.getRange("E2").setValue("Status:");
    const status = normalizeStatus(serie.status);
    if (status && sheet.getRange("F2").getDisplayValue() !== status) {
      sheet.getRange("F2").setValue(status);
    }

    // Atualiza Minutagens Fixas na linha 6 usando writeTimingAsText
    if (serie.fixedTimings) {
      if (serie.fixedTimings.opening !== undefined && serie.fixedTimings.opening !== "") {
        writeTimingAsText(sheet.getRange("A6"), serie.fixedTimings.opening);
      }
      if (serie.fixedTimings.recap !== undefined && serie.fixedTimings.recap !== "") {
        writeTimingAsText(sheet.getRange("B6"), serie.fixedTimings.recap);
      }
      if (serie.fixedTimings.ending !== undefined && serie.fixedTimings.ending !== "") {
        writeTimingAsText(sheet.getRange("C6"), serie.fixedTimings.ending);
      }
    }

    seriesProcessed++;

    const seasons = serie.seasons || [];
    if (seasons.length === 0) return;

    const displayValues = sheet.getDataRange().getDisplayValues();
    const existingRowMap = {}; // Chave "S1_E1" -> linha da planilha

    if (displayValues && displayValues.length >= 10) {
      for (let r = 9; r < displayValues.length; r++) {
        const sNum = parseSeasonNumber(displayValues[r][0]);
        const eNum = parseEpisodeNumber(displayValues[r][1]);
        if (sNum && eNum) {
          existingRowMap["S" + sNum + "_E" + eNum] = r + 1; // Linha 1-based
        }
      }
    }

    let nextAvailableRow = Math.max(10, displayValues.length + 1);

    seasons.forEach(season => {
      const seasonNumber = parseSeasonNumber(season.seasonNumber || season.number) || 1;
      const episodes = season.episodes || [];

      episodes.forEach(ep => {
        const episodeNumber = parseEpisodeNumber(ep.episodeNumber || ep.number) || 1;
        const key = "S" + seasonNumber + "_E" + episodeNumber;

        let targetRow = existingRowMap[key];
        let isNewRow = false;

        if (!targetRow) {
          targetRow = nextAvailableRow;
          nextAvailableRow++;
          existingRowMap[key] = targetRow;
          isNewRow = true;

          sheet.getRange(targetRow, 1).setValue("T" + (seasonNumber < 10 ? "0" + seasonNumber : seasonNumber));
          sheet.getRange(targetRow, 2).setValue("E" + (episodeNumber < 10 ? "0" + episodeNumber : episodeNumber));
          if (ep.title) sheet.getRange(targetRow, 3).setValue(ep.title);
        } else {
          if (ep.title && !sheet.getRange(targetRow, 3).getValue()) {
            sheet.getRange(targetRow, 3).setValue(ep.title);
          }
        }

        // Atualização explícita de Abertura (Cols D e E)
        updateEpisodeSectionCells(sheet, targetRow, 4, 5, ep.opening);

        // Atualização explícita de Recap (Cols F e G)
        updateEpisodeSectionCells(sheet, targetRow, 6, 7, ep.recap);

        // Atualização explícita de Encerramento (Cols H e I)
        updateEpisodeSectionCells(sheet, targetRow, 8, 9, ep.ending);

        // IDs por episódio (J e K). Vazio não apaga o que já existe,
        // e só escreve quando o valor da planilha é diferente (sincronização rápida).
        const currentRow = !isNewRow && displayValues[targetRow - 1] ? displayValues[targetRow - 1] : [];
        const currentImdb = currentRow[9] ? String(currentRow[9]).trim() : "";
        const currentTvdb = currentRow[10] ? String(currentRow[10]).trim() : "";
        if (ep.imdbId && String(ep.imdbId).trim() !== currentImdb) {
          sheet.getRange(targetRow, 10).setNumberFormat("@").setValue("'" + ep.imdbId);
        }
        if (ep.tvdbId && String(ep.tvdbId).trim() !== currentTvdb) {
          sheet.getRange(targetRow, 11).setNumberFormat("@").setValue("'" + ep.tvdbId);
        }

        episodesProcessed++;
      });
    });
  });

  return createJsonResponse({
    success: true,
    seriesCount: seriesProcessed,
    episodesCount: episodesProcessed,
    warnings: warnings
  });
}

/**
 * Atualiza um par de células (Início e Fim) de uma seção de minutagem
 */
function updateEpisodeSectionCells(sheet, row, colStart, colEnd, timingData) {
  if (!timingData) return;

  // 1. App pendente/vazio -> Preserva a célula existente na planilha sem alterar
  if (!timingData.isFilled && !timingData.hasNot && !timingData.startTime && !timingData.endTime && !timingData.durationFormatted) {
    return;
  }

  // 2. App com "Não possui" explícito -> Escreve "Não possui"
  if (timingData.hasNot || timingData.durationFormatted === "Não possui") {
    writeTimingAsText(sheet.getRange(row, colStart), "Não possui");
    writeTimingAsText(sheet.getRange(row, colEnd), "Não possui");
    return;
  }

  // 3. App preenchido com horários -> Escreve horários (substituindo "Não possui" ou valores anteriores)
  if (timingData.isFilled || timingData.startTime || timingData.endTime) {
    if (timingData.startTime) {
      writeTimingAsText(sheet.getRange(row, colStart), timingData.startTime);
    }
    if (timingData.endTime) {
      writeTimingAsText(sheet.getRange(row, colEnd), timingData.endTime);
    }
  }
}

/**
 * ============================================================================
 * 5. FUNÇÕES AUXILIARES E ESTRUTURAÇÃO DA PLANILHA
 * ============================================================================
 */
function getSpreadsheet(spreadsheetId) {
  if (spreadsheetId) {
    return SpreadsheetApp.openById(spreadsheetId);
  }
  return SpreadsheetApp.getActiveSpreadsheet();
}

function getSheetByTmdbId(ss, tmdbId) {
  const sheets = ss.getSheets();
  const normalizedSearch = normalizeTmdbId(tmdbId);

  for (let i = 0; i < sheets.length; i++) {
    const sheet = sheets[i];
    if (sheet.isSheetHidden()) continue; // nunca escreve em aba oculta (backup/cópia)
    const name = sheet.getName();
    if (normalizeTmdbId(name) === normalizedSearch) return sheet;

    const b1Val = sheet.getRange("B1").getDisplayValue();
    if (normalizeTmdbId(b1Val) === normalizedSearch) return sheet;
  }
  return null;
}

function setupNewSheetLayout(sheet, serie) {
  sheet.getRange("A1").setValue("TMDB ID:");
  sheet.getRange("B1").setNumberFormat("@").setValue("'" + normalizeTmdbId(serie.tmdbId || serie.id));
  sheet.getRange("C1").setValue("Ano:");
  sheet.getRange("D1").setValue(serie.releaseYear || "");
  sheet.getRange("E1").setValue("IMDb ID:");
  sheet.getRange("F1").setNumberFormat("@").setValue(serie.imdbId ? "'" + serie.imdbId : "");
  sheet.getRange("A2").setValue("Título:");
  sheet.getRange("B2").setValue(serie.title || "Série");
  sheet.getRange("E2").setValue("Status:");
  sheet.getRange("F2").setValue(normalizeStatus(serie.status));

  sheet.getRange("A4").setValue("MINUTAGEM FIXA");

  sheet.getRange("A5").setValue("Abertura");
  sheet.getRange("B5").setValue("Recap");
  sheet.getRange("C5").setValue("Encerramento");

  const headers = [
    "Temporada", "Episódio", "Título",
    "Abertura Início", "Abertura Fim",
    "Recap Início", "Recap Fim",
    "Encerramento Início", "Encerramento Fim",
    "IMDb ID", "TVDB ID"
  ];
  sheet.getRange("A9:K9").setValues([headers]);
}

function normalizeTmdbId(raw) {
  if (raw === null || raw === undefined) return "";
  let str = String(raw).trim();
  if (!str) return "";
  if (str.endsWith(".0")) {
    str = str.substring(0, str.length - 2);
  }
  return str;
}

function parseYearNumber(raw) {
  if (!raw) return 0;
  const digits = String(raw).replace(/\D/g, "").substring(0, 4);
  return parseInt(digits, 10) || 0;
}

function parseSeasonNumber(raw) {
  if (!raw) return 1;
  const digits = String(raw).replace(/\D/g, "");
  return parseInt(digits, 10) || 1;
}

function parseEpisodeNumber(raw) {
  if (!raw) return 1;
  const digits = String(raw).replace(/\D/g, "");
  return parseInt(digits, 10) || 1;
}

function buildTimingObject(start, end) {
  if (start === "Não possui" || end === "Não possui") {
    return { start: "Não possui", end: "Não possui" };
  }
  if (start || end) {
    return { start: start, end: end };
  }
  return { start: "", end: "" };
}

function createJsonResponse(data) {
  return ContentService.createTextOutput(JSON.stringify(data))
    .setMimeType(ContentService.MimeType.JSON);
}

/**
 * ============================================================================
 * 6. LIMPEZA DE EPISÓDIOS (clearEpisodes)
 * ============================================================================
 * Limpa estritamente as células D:I dos episódios selecionados.
 * Utiliza tryLock() para controle de concorrência com LockService.
 */
function handleClearEpisodes(payload, ss) {
  var lock = LockService.getScriptLock();
  var lockAcquired = lock.tryLock(10000);

  if (!lockAcquired) {
    return {
      success: false,
      action: "clearEpisodes",
      tmdbId: payload ? String(payload.tmdbId || "") : "",
      requestedEpisodes:
        payload && Array.isArray(payload.episodes)
          ? payload.episodes.length
          : 0,
      foundEpisodes: 0,
      clearedEpisodes: 0,
      verifiedEmptyCells: 0,
      message: "Não foi possível iniciar a limpeza porque existe outra operação em andamento."
    };
  }

  try {
    var tmdbId = normalizeTmdbIdForClear(payload ? payload.tmdbId : "");
    var seriesTitle = String((payload && payload.seriesTitle) || "").trim();
    var rawEpisodes = payload ? payload.episodes : null;

    var logs = [];
    logs.push("action: clearEpisodes");
    logs.push("tmdbId: " + tmdbId);
    logs.push("título: " + seriesTitle);

    // 1. Validação estrita do array de episódios: recusa solicitações vazias ou inválidas
    if (!Array.isArray(rawEpisodes) || rawEpisodes.length === 0) {
      logs.push("erro: lista de episódios inválida ou vazia");
      Logger.log(logs.join(" | "));
      return {
        success: false,
        action: "clearEpisodes",
        tmdbId: tmdbId,
        requestedEpisodes: 0,
        foundEpisodes: 0,
        clearedEpisodes: 0,
        verifiedEmptyCells: 0,
        message: "Nenhum episódio informado para a limpeza."
      };
    }

    var requestedCount = rawEpisodes.length;
    logs.push("quantidade solicitada: " + requestedCount);

    if (!tmdbId) {
      return {
        success: false,
        action: "clearEpisodes",
        tmdbId: tmdbId,
        requestedEpisodes: requestedCount,
        foundEpisodes: 0,
        clearedEpisodes: 0,
        verifiedEmptyCells: 0,
        message: "TMDB ID não informado para a limpeza."
      };
    }

    // 2. Busca a aba existente exclusivamente pela célula B1
    var sheet = findSheetByTmdbIdForClear(ss, tmdbId, seriesTitle);
    if (!sheet) {
      logs.push("nome da aba encontrada: NENHUMA");
      Logger.log(logs.join(" | "));
      return {
        success: false,
        action: "clearEpisodes",
        tmdbId: tmdbId,
        requestedEpisodes: requestedCount,
        foundEpisodes: 0,
        clearedEpisodes: 0,
        verifiedEmptyCells: 0,
        message: "Nenhuma aba encontrada para o TMDB ID " + tmdbId
      };
    }

    logs.push("nome da aba encontrada: " + sheet.getName());

    var lastRow = sheet.getLastRow();
    var foundCount = 0;
    var clearedCount = 0;
    var verifiedEmptyCellsCount = 0;

    if (lastRow >= 10 && requestedCount > 0) {
      var numRows = lastRow - 9;
      var rangeAB = sheet.getRange(10, 1, numRows, 2).getValues();

      for (var k = 0; k < rawEpisodes.length; k++) {
        var targetEp = rawEpisodes[k];
        var targetSeasonNum = parseNumberForClear(targetEp ? (targetEp.seasonNumber || targetEp.season) : null);
        var targetEpNum = parseNumberForClear(targetEp ? (targetEp.episodeNumber || targetEp.episode) : null);

        // 3. Validação estrita dos números: descarta nulos ou <= 0
        if (targetSeasonNum === null || targetSeasonNum <= 0 || targetEpNum === null || targetEpNum <= 0) {
          logs.push("episódio na posição " + k + " ignorado por ter números inválidos");
          continue;
        }

        var foundRow = -1;

        // Busca simultânea nas Colunas A (Temporada) e B (Episódio)
        for (var r = 0; r < rangeAB.length; r++) {
          var sNum = parseNumberForClear(rangeAB[r][0]);
          var eNum = parseNumberForClear(rangeAB[r][1]);

          // Impede comparação de valores nulos com células vazias da planilha
          if (sNum !== null && sNum > 0 && eNum !== null && eNum > 0) {
            if (sNum === targetSeasonNum && eNum === targetEpNum) {
              foundRow = 10 + r;
              break;
            }
          }
        }

        if (foundRow > 0) {
          foundCount++;

          // Limpeza explícita das células D:I (Coluna 4, 1 linha, 6 colunas)
          sheet.getRange(foundRow, 4, 1, 6).clearContent();
          clearedCount++;

          // 4. Re-verificação real: Lê e confirma que as 6 células estão vazias
          var recheckValues = sheet.getRange(foundRow, 4, 1, 6).getValues()[0];
          var emptyInRow = 0;
          for (var c = 0; c < recheckValues.length; c++) {
            var cellVal = recheckValues[c];
            if (cellVal === "" || cellVal === null || cellVal === undefined) {
              emptyInRow++;
            }
          }
          verifiedEmptyCellsCount += emptyInRow;

          logs.push("linha " + foundRow + " (T" + targetSeasonNum + "E" + targetEpNum + ") -> " + emptyInRow + "/6 células confirmadas vazias");
        } else {
          logs.push("episódio T" + targetSeasonNum + "E" + targetEpNum + " -> NÃO encontrado");
        }
      }
    }

    logs.push("linhas encontradas: " + foundCount);
    logs.push("células confirmadas vazias: " + verifiedEmptyCellsCount);
    Logger.log(logs.join(" | "));

    var expectedCells = requestedCount * 6;
    var allFound = (foundCount === requestedCount);
    var allCleared = (clearedCount === requestedCount);
    var allVerified = (verifiedEmptyCellsCount === expectedCells);

    return {
      success: allFound && allCleared && allVerified,
      action: "clearEpisodes",
      tmdbId: tmdbId,
      sheetName: sheet.getName(),
      requestedEpisodes: requestedCount,
      foundEpisodes: foundCount,
      clearedEpisodes: clearedCount,
      verifiedEmptyCells: verifiedEmptyCellsCount,
      message: (allFound && allCleared && allVerified)
        ? "Episódios limpos e confirmados na planilha"
        : "Incompleto: " + foundCount + " de " + requestedCount + " episódios limpos"
    };

  } catch (err) {
    return {
      success: false,
      action: "clearEpisodes",
      tmdbId: payload ? String(payload.tmdbId || "") : "",
      requestedEpisodes: (payload && Array.isArray(payload.episodes)) ? payload.episodes.length : 0,
      foundEpisodes: 0,
      clearedEpisodes: 0,
      verifiedEmptyCells: 0,
      message: "Erro ao executar limpeza: " + (err.message || err.toString())
    };
  } finally {
    if (lockAcquired) {
      try {
        lock.releaseLock();
      } catch (e) {
        Logger.log("Falha ao liberar a trava: " + e);
      }
    }
  }
}

/**
 * Funções auxiliares dedicadas com sufixo "ForClear" para evitar qualquer conflito
 */
function findSheetByTmdbIdForClear(ss, tmdbId, defaultTitle) {
  var cleanTmdbId = normalizeTmdbIdForClear(tmdbId);
  if (!cleanTmdbId) return null;

  var sheets = ss.getSheets();
  var ignoredSheets = ["MODELO V2", "MODELO", "INSTRUÇÕES", "INSTRUCOES", "CONFIGURAÇÕES"];

  for (var i = 0; i < sheets.length; i++) {
    var sheet = sheets[i];
    var sheetName = sheet.getName().trim();
    if (ignoredSheets.indexOf(sheetName.toUpperCase()) !== -1) continue;
    if (sheet.isSheetHidden()) continue; // nunca limpa aba oculta (backup/cópia)

    var cellB1 = normalizeTmdbIdForClear(sheet.getRange("B1").getValue());
    if (cellB1 === cleanTmdbId) {
      // Renomeia a aba de forma segura se estiver nomeada como o TMDB ID (ex: "256201")
      if (sheetName === cleanTmdbId) {
        var realTitle = String(sheet.getRange("B2").getValue() || "").trim() || defaultTitle;
        if (realTitle && realTitle !== cleanTmdbId) {
          var safeName = sanitizeSheetNameForClear(realTitle);
          if (!ss.getSheetByName(safeName)) {
            try {
              sheet.setName(safeName);
              Logger.log("Aba renomeada com segurança para " + safeName);
            } catch (e) {}
          }
        }
      }
      return sheet;
    }
  }

  return null;
}

function normalizeTmdbIdForClear(raw) {
  if (raw === null || raw === undefined) return "";
  var str = String(raw).trim();
  if (str.endsWith(".0")) {
    str = str.substring(0, str.length - 2);
  }
  return str;
}

function parseNumberForClear(raw) {
  if (raw === null || raw === undefined) return null;
  var str = String(raw).replace(/[^0-9]/g, "");
  if (!str) return null;
  var num = parseInt(str, 10);
  return isNaN(num) ? null : num;
}

function sanitizeSheetNameForClear(name) {
  if (!name) return "Série Sem Nome";
  var clean = name.replace(/[\/\\?\*:\[\]]/g, " ").trim();
  if (clean.length > 31) {
    clean = clean.substring(0, 31).trim();
  }
  return clean || "Série";
}


/**
 * ============================================================================
 * FORMATADOR AUTOMÁTICO: só deixa as abas bonitas, não altera os dados.
 * ============================================================================
 */
const FORMATADOR = Object.freeze({
  propriedadeId: 'SPREADSHEET_ID',
  // Mude este texto sempre que o visual mudar: todas as abas são refeitas sozinhas uma vez.
  versaoVisual: 'v1.10-status-3',
  abasIgnoradas: [
    'MODELO', 'MODELO V2', 'INSTRUÇÕES', 'INSTRUCOES',
    'CONFIGURAÇÕES', 'CONFIGURACOES'
  ],
  corFundo: '#11151A',
  corLinhaAlternada: '#151A20',
  corGrade: '#2A3038',
  corTexto: '#F4F6F8',
  vermelho: '#B51227',
  vermelhoEscuro: '#65111C',
  abertura: '#35171C',
  recap: '#2D1C2D',
  encerramento: '#14271F',
  // Cor de cada status em F2: [fundo, texto] (mesmos tons do selo do app)
  status: {
    'Em andamento': ['#13263D', '#7DB4F5'],
    'Finalizada': ['#14271F', '#5FD08A'],
    'Incompleta': ['#3A2C12', '#F2B84B'],
    'Em breve': ['#1E2229', '#A9B1BC']
  }
});

/** Execute uma vez para criar o acionador automático de 1 minuto. */
function instalarFormatadorAutomatico() {
  validarConfiguracao_();

  ScriptApp.getProjectTriggers()
    .filter(t => t.getHandlerFunction() === 'formatarAutomaticamente')
    .forEach(t => ScriptApp.deleteTrigger(t));

  ScriptApp.newTrigger('formatarAutomaticamente')
    .timeBased()
    .everyMinutes(1)
    .create();

  formatarAutomaticamente(true);
  return 'Formatador instalado e primeira formatação concluída.';
}

/** Executada pelo acionador. Use true para forçar todas as abas. */
function formatarAutomaticamente(forcar) {
  const lock = LockService.getScriptLock();
  // Na execução manual espera mais, para não desistir calado enquanto o acionador de 1 minuto roda.
  if (!lock.tryLock(forcar === true ? 120000 : 20000)) {
    if (forcar === true) throw new Error('O formatador automático está rodando agora. Espere um minuto e tente de novo.');
    return;
  }

  const relatorio = [];
  try {
    const planilha = SpreadsheetApp.openById(validarConfiguracao_());
    const props = PropertiesService.getScriptProperties();

    planilha.getSheets().forEach(aba => {
      if (!ehAbaDeSerie_(aba)) {
        relatorio.push('Ignorada: ' + aba.getName());
        return;
      }

      renomearAbaTmdb_(aba, planilha);

      const chave = 'FORMATADA_' + aba.getSheetId();
      const assinatura = [FORMATADOR.versaoVisual, aba.getLastRow(), aba.getMaxRows(), aba.getMaxColumns()].join(':');

      if (forcar === true || props.getProperty(chave) !== assinatura) {
        try {
          aplicarVisual_(aba);
          props.setProperty(chave, assinatura);
          relatorio.push('Formatada: ' + aba.getName());
        } catch (erro) {
          // uma aba com problema não impede as outras
          relatorio.push('ERRO em ' + aba.getName() + ': ' + erro.message);
        }
      }
    });
  } finally {
    lock.releaseLock();
  }
  Logger.log(relatorio.join(' | '));
  return relatorio.join(' | ');
}

/** Execute manualmente se quiser reaplicar o visual em todas as séries. */
function reaplicarFormatacaoCompleta() {
  return formatarAutomaticamente(true);
}

function validarConfiguracao_() {
  // Quem colou o código dentro da planilha não precisa configurar nada: usa a própria planilha.
  const id = PropertiesService.getScriptProperties().getProperty(FORMATADOR.propriedadeId);
  if (id) return id.trim();
  const ativa = SpreadsheetApp.getActiveSpreadsheet();
  if (ativa) return ativa.getId();
  throw new Error('Cole este código em Extensões > Apps Script dentro da sua planilha.');
}

/** Cria o acionador do formatador na primeira sincronização, se ainda não existir. */
function garantirFormatador_() {
  try {
    const props = PropertiesService.getScriptProperties();
    if (props.getProperty('FORMATADOR_INSTALADO') === 'sim') return;
    const existe = ScriptApp.getProjectTriggers()
      .some(t => t.getHandlerFunction() === 'formatarAutomaticamente');
    if (!existe) {
      ScriptApp.newTrigger('formatarAutomaticamente').timeBased().everyMinutes(1).create();
    }
    props.setProperty('FORMATADOR_INSTALADO', 'sim');
  } catch (erro) {
    // sem permissão para criar o acionador: os dados continuam salvos, só sem as cores automáticas
    Logger.log('Formatador não instalado: ' + erro.message);
  }
}

function ehAbaDeSerie_(aba) {
  const nome = normalizar_(aba.getName());
  if (FORMATADOR.abasIgnoradas.map(normalizar_).includes(nome)) return false;
  if (aba.getMaxColumns() < 11 || aba.getMaxRows() < 10) return false;

  const tmdbId = String(aba.getRange('B1').getDisplayValue()).trim();
  const titulo = String(aba.getRange('B2').getDisplayValue()).trim();
  const cabecalhos = aba.getRange('A9:K9').getDisplayValues()[0].map(normalizar_);

  return Boolean(tmdbId && titulo &&
    cabecalhos[0] === 'TEMPORADA' &&
    cabecalhos[1] === 'EPISODIO' &&
    cabecalhos[2] === 'TITULO');
}

function aplicarVisual_(aba) {
  const maxLinhas = aba.getMaxRows();
  const linhasEpisodios = Math.max(1, maxLinhas - 9);

  // Aparência geral
  aba.setHiddenGridlines(true);
  aba.setFrozenRows(9);
  aba.setTabColor(FORMATADOR.vermelho);

  // Larguras: A até K
  [76, 76, 355, 116, 116, 116, 116, 140, 140, 100, 90]
    .forEach((largura, i) => aba.setColumnWidth(i + 1, largura));

  // Alturas compactas
  aba.setRowHeights(1, 2, 24);
  aba.setRowHeight(3, 8);
  aba.setRowHeight(4, 25);
  aba.setRowHeights(5, 2, 24);
  aba.setRowHeights(7, 2, 8);
  aba.setRowHeight(9, 30);
  aba.setRowHeights(10, linhasEpisodios, 22);

  // Fundo, fonte e bordas de A:K
  aba.getRange(1, 1, maxLinhas, 11)
    .setFontFamily('Arial')
    .setFontSize(10)
    .setFontColor(FORMATADOR.corTexto)
    .setVerticalAlignment('middle')
    .setBackground(FORMATADOR.corFundo)
    .setBorder(true, true, true, true, true, true, FORMATADOR.corGrade, SpreadsheetApp.BorderStyle.SOLID);

  // Metadados superiores
  aba.getRange('A1:A2').setBackground(FORMATADOR.vermelho).setFontWeight('bold');
  aba.getRange('C1:C1').setBackground(FORMATADOR.vermelho).setFontWeight('bold');
  aba.getRange('E1:E2').setBackground(FORMATADOR.vermelho).setFontWeight('bold');
  aba.getRange('A4:C4').setBackground(FORMATADOR.vermelho).setFontWeight('bold');
  aba.getRange('A5:C5').setBackground(FORMATADOR.vermelhoEscuro).setFontWeight('bold');

  // Status da série (F2): lista de escolha e destaque. Não muda o valor da célula.
  aplicarVisualStatus_(aba);

  // Cabeçalho da tabela
  aba.getRange('A9:K9')
    .setBackground(FORMATADOR.vermelho)
    .setFontWeight('bold')
    .setHorizontalAlignment('center');

  // Linhas compactas e grupos visuais sem alterar conteúdo
  const fundos = [];
  for (let i = 0; i < linhasEpisodios; i++) {
    const neutro = i % 2 === 0 ? FORMATADOR.corFundo : FORMATADOR.corLinhaAlternada;
    fundos.push([
      neutro, neutro, neutro,
      FORMATADOR.abertura, FORMATADOR.abertura,
      FORMATADOR.recap, FORMATADOR.recap,
      FORMATADOR.encerramento, FORMATADOR.encerramento,
      neutro, neutro
    ]);
  }

  aba.getRange(10, 1, linhasEpisodios, 11)
    .setBackgrounds(fundos)
    .setFontWeight('normal');

  aba.getRange(10, 1, linhasEpisodios, 2).setHorizontalAlignment('center');
  aba.getRange(10, 3, linhasEpisodios, 1).setHorizontalAlignment('left');
  aba.getRange(10, 4, linhasEpisodios, 8).setHorizontalAlignment('center');

  // Regras visuais dinâmicas. Elas mudam a aparência, nunca os valores.
  // As regras antigas deste formatador ("Não possui" e as cores do F2) saem antes de serem recriadas.
  const faixaMinutagens = aba.getRange(10, 4, linhasEpisodios, 6);
  const outrasRegras = aba.getConditionalFormatRules().filter(regra => {
    if (regra.getRanges().some(r => r.getA1Notation() === 'F2')) return false;
    const condicao = regra.getBooleanCondition();
    if (!condicao) return true;
    const valores = condicao.getCriteriaValues().map(String);
    return !(condicao.getCriteriaType() === SpreadsheetApp.BooleanCriteria.TEXT_EQUAL_TO &&
      valores.includes('Não possui'));
  });

  const regraNaoPossui = SpreadsheetApp.newConditionalFormatRule()
    .whenTextEqualTo('Não possui')
    .setFontColor('#8E97A3')
    .setBackground('#15191F')
    .setRanges([faixaMinutagens])
    .build();

  const celulaStatus = aba.getRange('F2');
  const regrasStatus = Object.keys(FORMATADOR.status).map(nome =>
    SpreadsheetApp.newConditionalFormatRule()
      .whenTextEqualTo(nome)
      .setBackground(FORMATADOR.status[nome][0])
      .setFontColor(FORMATADOR.status[nome][1])
      .setBold(true)
      .setRanges([celulaStatus])
      .build()
  );

  aba.setConditionalFormatRules([...outrasRegras, regraNaoPossui, ...regrasStatus]);
}

/**
 * F2 vira uma lista de escolha com os 4 status do app.
 * Valores fora da lista não são aceitos (evita erro de digitação que o app ignoraria).
 */
function aplicarVisualStatus_(aba) {
  // Abas antigas têm células mescladas em cima de E2/F2: a mesclagem esconde o rótulo e a lista.
  // Desfaz só as mesclagens que encostam em E2:F2 (o valor continua na 1ª célula, nada se perde).
  aba.getRange('E2:F2').getMergedRanges().forEach(m => m.breakApart());
  const celula = aba.getRange('F2');
  celula
    .setHorizontalAlignment('center')
    .setFontWeight('bold')
    .setDataValidation(
      SpreadsheetApp.newDataValidation()
        .requireValueInList(Object.keys(FORMATADOR.status), true)
        .setAllowInvalid(false)
        .setHelpText('Status da série no app: Em andamento, Finalizada, Incompleta ou Em breve.')
        .build()
    );
}

/**
 * Renomeia somente quando o nome da aba é o próprio TMDB ID de B1.
 * Não altera nenhuma célula.
 */
function renomearAbaTmdb_(aba, planilha) {
  const nomeAtual = String(aba.getName()).trim();
  const tmdbId = String(aba.getRange('B1').getDisplayValue()).trim();
  const titulo = String(aba.getRange('B2').getDisplayValue()).trim();

  if (!/^\d+$/.test(nomeAtual) || nomeAtual !== tmdbId || !titulo) return;

  const nomeBase = titulo
    .replace(/[\\\/\?\*\[\]:]/g, ' ')
    .replace(/\s+/g, ' ')
    .trim()
    .slice(0, 100);

  if (!nomeBase) return;

  let novoNome = nomeBase;
  let contador = 2;
  while (planilha.getSheetByName(novoNome) &&
         planilha.getSheetByName(novoNome).getSheetId() !== aba.getSheetId()) {
    const sufixo = ' (' + contador++ + ')';
    novoNome = nomeBase.slice(0, 100 - sufixo.length) + sufixo;
  }

  aba.setName(novoNome);
}

function normalizar_(texto) {
  return String(texto || '')
    .normalize('NFD')
    .replace(/[̀-ͯ]/g, '')
    .trim()
    .toUpperCase();
}
